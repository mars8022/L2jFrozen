/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import javolution.util.FastMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jserver.script.jython.JythonScriptEngine;

/**
 * Caches script engines and provides functionality for executing and managing scripts.<BR>
 * 
 * @author KenM
 */
public final class L2ScriptEngineManager
{
	private static final Logger _log = Logger.getLogger(L2ScriptEngineManager.class.getName());

	public final static File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");

	public static L2ScriptEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<String, ScriptEngine> _nameEngines = new FastMap<String, ScriptEngine>();
	private final Map<String, ScriptEngine> _extEngines = new FastMap<String, ScriptEngine>();
	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<ScriptManager<?>>();

	private final CompiledScriptCache _cache;

	private File _currentLoadingScript;

	private L2ScriptEngineManager()
	{
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		if(Config.SCRIPT_CACHE)
		{
			_cache = loadCompiledScriptCache();
		}
		else
		{
			_cache = null;
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CleaneCache(), 43200000, 43200000);
		_log.info("Initializing Script Engine Manager");

		for(ScriptEngineFactory factory : factories)
		{
			try
			{
				ScriptEngine engine = factory.getScriptEngine();
				boolean reg = false;
				for(String name : factory.getNames())
				{
					ScriptEngine existentEngine = _nameEngines.get(name);

					if(existentEngine != null)
					{
						double engineVer = Double.parseDouble(factory.getEngineVersion());
						double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());

						if(engineVer <= existentEngVer)
						{
							continue;
						}
					}

					reg = true;
					_nameEngines.put(name, engine);
				}

				if(reg)
				{
					_log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
				}

				for(String ext : factory.getExtensions())
				{
					if(!ext.equals("java") || factory.getLanguageName().equals("java"))
					{
						_extEngines.put(ext, engine);
					}
				}
			}
			catch(Exception e)
			{
				_log.warning("Failed initializing factory. ");
				e.printStackTrace();
			}
		}

		preConfigure();
	}

	private void preConfigure()
	{
		// Jython sys.path
		String dataPackDirForwardSlashes = SCRIPT_FOLDER.getPath().replaceAll("\\\\", "/");
		String configScript = "import sys;sys.path.insert(0,'" + dataPackDirForwardSlashes + "');";
		try
		{
			this.eval("jython", configScript);
		}
		catch(ScriptException e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.severe("Failed preconfiguring jython: " + e.getMessage());
		}
	}

	private ScriptEngine getEngineByName(String name)
	{
		return _nameEngines.get(name);
	}

	private ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}

	public void executeScriptsList(File list) throws IllegalArgumentException
	{
		if(list.isFile())
		{
			FileInputStream reader = null;
			InputStreamReader buff = null;
			LineNumberReader lnr = null;
			
			try
			{
				reader = new FileInputStream(list);
				buff = new InputStreamReader(reader);
				lnr = new LineNumberReader(buff);

				String line;
				File file;

				while((line = lnr.readLine()) != null)
				{
					String[] parts = line.trim().split("#");

					if(parts.length > 0 && !parts[0].startsWith("#") && parts[0].length() > 0)
					{
						line = parts[0];

						if(line.endsWith("/**"))
						{
							line = line.substring(0, line.length() - 3);
						}
						else if(line.endsWith("/*"))
						{
							line = line.substring(0, line.length() - 2);
						}

						file = new File(SCRIPT_FOLDER, line);

						if(file.isDirectory() && parts[0].endsWith("/**"))
						{
							this.executeAllScriptsInDirectory(file, true, 32);
						}
						else if(file.isDirectory() && parts[0].endsWith("/*"))
						{
							this.executeAllScriptsInDirectory(file);
						}
						else if(file.isFile())
						{
							try
							{
								this.executeScript(file);
							}
							catch(ScriptException e)
							{
								if(Config.ENABLE_ALL_EXCEPTIONS)
									e.printStackTrace();
								
								reportScriptFileError(file, e);
							}
						}
						else
						{
							_log.warning("Failed loading: (" + file.getCanonicalPath() + ") @ " + list.getName() + ":" + lnr.getLineNumber() + " - Reason: doesnt exists or is not a file.");
						}
					}
				}
				
			}
			catch(FileNotFoundException e1)
			{
				e1.printStackTrace();
			
			}
			catch(IOException e)
			{
				e.printStackTrace();
				
			}finally
			{
				if(lnr != null)
					try
					{
						lnr.close();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
				
				if(buff != null)
					try
					{
						buff.close();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
				
				if(reader != null)
					try
					{
						reader.close();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
				
			}
			
		}else
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
	}

	public void executeAllScriptsInDirectory(File dir)
	{
		this.executeAllScriptsInDirectory(dir, false, 0);
	}

	public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth)
	{
		this.executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0);
	}

	private void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, int currentDepth)
	{
		if(dir.isDirectory())
		{
			for(File file : dir.listFiles())
			{
				if(file.isDirectory() && recurseDown && maxDepth > currentDepth)
				{
					if(Config.SCRIPT_DEBUG)
					{
						_log.info("Entering folder: " + file.getName());
					}
					this.executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1);
				}
				else if(file.isFile())
				{
					try
					{
						String name = file.getName();
						int lastIndex = name.lastIndexOf('.');
						String extension;
						if(lastIndex != -1)
						{
							extension = name.substring(lastIndex + 1);
							ScriptEngine engine = getEngineByExtension(extension);
							if(engine != null)
							{
								this.executeScript(engine, file);
							}
						}
					}
					catch(ScriptException e)
					{
						if(Config.ENABLE_ALL_EXCEPTIONS)
							e.printStackTrace();
						
						reportScriptFileError(file, e);
						//e.printStackTrace();
					}
				}
			}
		}
		else
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
	}

	public CompiledScriptCache getCompiledScriptCache()
	{
		return _cache;
	}

	public CompiledScriptCache loadCompiledScriptCache()
	{
		CompiledScriptCache cache = null;
		
		if(Config.SCRIPT_CACHE)
		{
			File file = new File(SCRIPT_FOLDER, "CompiledScripts.cache");
			if(file.isFile())
			{
				FileInputStream fis = null;
				ObjectInputStream ois = null;
				try
				{
					fis = new FileInputStream(file);
					ois = new ObjectInputStream(fis);
					cache = (CompiledScriptCache) ois.readObject();
				}
				catch(InvalidClassException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					_log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache, invalid class (Possibly outdated).", e);
				}
				catch(IOException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					_log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache from file.", e);
				}
				catch(ClassNotFoundException e)
				{
					if(Config.ENABLE_ALL_EXCEPTIONS)
						e.printStackTrace();
					_log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache, class not found.", e);
				}
				finally
				{
					if(ois != null)
						try
						{
							ois.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					if(fis != null)
						try
						{
							fis.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
				}
				
			}
			
			if(cache == null){
				cache = new CompiledScriptCache();
			}
			
		}
		
		return cache;
	}

	protected class CleaneCache implements Runnable
	{
		@Override
		public void run()
		{

		}
	}

	public void executeScript(File file) throws ScriptException
	{
		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		String extension;
		if(lastIndex != -1)
		{
			extension = name.substring(lastIndex + 1);
		}
		else
			throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");

		ScriptEngine engine = getEngineByExtension(extension);
		if(engine == null)
			throw new ScriptException("No engine registered for extension (" + extension + ")");
		executeScript(engine, file);
	}

	public void executeScript(String engineName, File file) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if(engine == null)
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		executeScript(engine, file);
	}

	public void executeScript(ScriptEngine engine, File file) throws ScriptException
	{
		
		FileInputStream reader = null;
		InputStreamReader buff = null;
		BufferedReader lnr = null;
		
		try
		{
			reader = new FileInputStream(file);
			buff = new InputStreamReader(reader);
			lnr = new BufferedReader(buff);

			if(Config.SCRIPT_DEBUG)
			{
				_log.info("Loading Script: " + file.getAbsolutePath());
			}

			if(Config.SCRIPT_ERROR_LOG)
			{
				String name = file.getAbsolutePath() + ".error.log";
				File errorLogger = new File(name);
				if(errorLogger.isFile())
				{
					errorLogger.delete();
				}
			}

			if(engine instanceof Compilable && Config.SCRIPT_ALLOW_COMPILATION)
			{
				ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, engine, ScriptContext.ENGINE_SCOPE);

				setCurrentLoadingScript(file);
				ScriptContext ctx = engine.getContext();
				try
				{
					engine.setContext(context);
					if(Config.SCRIPT_CACHE)
					{
						CompiledScript cs = _cache.loadCompiledScript(engine, file);
						cs.eval(context);
					}
					else
					{
						Compilable eng = (Compilable) engine;
						CompiledScript cs = eng.compile(lnr);
						cs.eval(context);
					}
				}
				finally
				{
					engine.setContext(ctx);
					setCurrentLoadingScript(null);
					context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
					context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
				}
			}
			else
			{
				ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				setCurrentLoadingScript(file);
				try
				{
					engine.eval(lnr, context);
				}
				finally
				{
					setCurrentLoadingScript(null);
					engine.getContext().removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
					engine.getContext().removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
				}

			}
			
		}catch(IOException e){
		
			e.printStackTrace();
		
		}
		finally
		{
			if(lnr != null)
				try
				{
					lnr.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(buff != null)
				try
				{
					buff.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
			if(reader != null)
				try
				{
					reader.close();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			
		}
		
	}

	public static String getClassForFile(File script)
	{
		String path = script.getAbsolutePath();
		String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		if(path.startsWith(scpPath))
		{
			int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx);
		}
		return null;
	}

	public ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}

	public ScriptContext getScriptContext(String engineName)
	{
		ScriptEngine engine = getEngineByName(engineName);
		if(engine == null)
			throw new IllegalStateException("No engine registered with name (" + engineName + ")");
		return getScriptContext(engine);
	}

	public Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if(engine instanceof Compilable && Config.SCRIPT_ALLOW_COMPILATION)
		{
			Compilable eng = (Compilable) engine;
			CompiledScript cs = eng.compile(script);
			return context != null ? cs.eval(context) : cs.eval();
		}
		return context != null ? engine.eval(script, context) : engine.eval(script);
	}

	public Object eval(String engineName, String script) throws ScriptException
	{
		return this.eval(engineName, script, null);
	}

	public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if(engine == null)
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		return eval(engine, script, context);
	}

	public Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return eval(engine, script, null);
	}

	public void reportScriptFileError(File script, ScriptException e)
	{
		String dir = script.getParent();
		String name = script.getName() + ".error.log";
		if(dir != null)
		{
			File file = new File(dir + "/" + name);
			FileOutputStream fos = null;
			try
			{
				if(!file.exists())
				{
					file.createNewFile();
				}

				fos = new FileOutputStream(file);
				String errorHeader = "Error on: " + file.getCanonicalPath() + "\r\nLine: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + "\r\n\r\n";
				fos.write(errorHeader.getBytes());
				fos.write(e.getMessage().getBytes());
				_log.warning("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
			}
			catch(IOException ioe)
			{
				_log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage());
				ioe.printStackTrace();
			}
			finally
			{
				if(fos != null)
					try
					{
						fos.close();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
			}
		}
		else
		{
			_log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory.");
		}
	}

	public void registerScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.add(manager);
	}

	public void removeScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.remove(manager);
	}

	public List<ScriptManager<?>> getScriptManagers()
	{
		return _scriptManagers;

	}

	/**
	 * @param currentLoadingScript The currentLoadingScript to set.
	 */
	protected void setCurrentLoadingScript(File currentLoadingScript)
	{
		_currentLoadingScript = currentLoadingScript;
	}

	/**
	 * @return Returns the currentLoadingScript.
	 */
	protected File getCurrentLoadScript()
	{
		return _currentLoadingScript;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final L2ScriptEngineManager _instance = new L2ScriptEngineManager();
	}
}
