/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.services;

/**
 * @author Bruno Gambier
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeService
{
	// Nome do job
	private String nomeJob = "Teste Backup";
	// Historico sobre a execu� do job
	String historico;
	// Descri� do backup
	private String descricao = "Backup full";
	// inicio da execu� - hh:mm:ss
	@SuppressWarnings("unused")
	private String startTime = "23:59:59";
	// ultima execu�* - hh:mm:ss
	private String lastRunTime = "000000";
	// Data da ultima execu�
	private String lastRunDate = "2006-05-12 15:35:03";
	String a[] = lastRunDate.split("-");
	// Hora/Minuto da pr�a execu�
	private String nextRunTime = "002000";
	// Data da pr�a execu�
	private String nextRunDate = "2006-05-13 18:25:10";
	// Tempo de dura� da ultima execu�
	private String lastDuration = "004000";
	// M�a de tempo de execu�
	private int medExec;
	// Endere�da imagem do fundo da p�na
	private String backGround;
	private Date last;
	private Date next;

	// Creates a new instance of backupmonitor
	public TimeService()
	{
	}

	// Creates a new instance of backupmonitor
	public TimeService(String nomeJob, String historico, String descricao, String lastRunTime, String lastRunDate, String nextRunTime, String nextRunDate, String lastDuration, int medExec, String backGround)
	{
		this.nomeJob = nomeJob;
		this.historico = historico;
		this.descricao = descricao;
		this.lastRunTime = lastRunTime;
		this.lastRunDate = lastRunDate;
		this.nextRunTime = nextRunTime;
		this.nextRunDate = nextRunDate;
		this.lastDuration = lastDuration;
		this.backGround = backGround;
		this.medExec = medExec;
	}

	/***********************************************************************
	 * public Greg(int year, int month, int dayOfMonth){ GregorianCalendar greg = new GregorianCalendar(); this.year = year; this.month = month; this.dayOfMonth = dayOfMonth; }
	 **********************************************************************/
	// SETs
	public void setNomeJob(String nomeJob)
	{
		setNomeJob(nomeJob);
	}

	public void setHistorico(String historico)
	{
		setHistorico(historico);
	}

	public void setDescricao(String descricao)
	{
		setDescricao(descricao);
	}

	public void setLastRunTime(String lastRunTime)
	{
		setLastRunTime(lastRunTime);
	}

	public void setLastRunDate(String lastRunDate)
	{
		setLastRunDate(lastRunDate);
	}

	public void setNextRunTime(String nextRunTime)
	{
		setNextRunTime(nextRunTime);
	}

	public void setNextRunDate(String nextRunDate)
	{
		setNextRunDate(nextRunDate);
	}

	public void setLastDuration(String lastDuration)
	{
		setLastDuration(lastDuration);
	}

	public void setBackGround(String backGround)
	{
		setBackGround(backGround);
	}

	public void setMedExec(int medExec)
	{
		setMedExec(medExec);
	}

	public void setDateNow(String dateNow)
	{
		setDateNow(dateNow);
	}

	public void setTimeNow(String timeNow)
	{
		setTimeNow(timeNow);
	}

	// GETs
	public String getNomeJob()
	{
		return nomeJob;
	}

	public String getHistorico()
	{
		return historico;
	}

	public String getDescricao()
	{
		return descricao;
	}

	public String getLastRunTime()
	{
		return lastRunTime;
	}

	public String getLastRunDate()
	{
		return lastRunDate;
	}

	public String getNextRunTime()
	{
		return nextRunTime;
	}

	public String getNextRunDate()
	{
		return nextRunDate;
	}

	public String getLastDuration()
	{
		return lastDuration;
	}

	public int getMedExec()
	{
		return medExec;
	}

	public String getBackGround()
	{
		return backGround;
	}

	/** Retorna a data e hor�o atual * */
	public static String Datenow()
	{
		@SuppressWarnings("unused")
		GregorianCalendar thisday = new GregorianCalendar();// instancia
		// objeto
		// Calen�o
		Date today = new Date();// instanciando o objeto Date
		@SuppressWarnings("unused")
		long todayLong = today.getTime(); // convertendo Date para Long
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //
		String daytoday = df.format(today);
		return daytoday;
	}

	public void ConvDt()
	{
		@SuppressWarnings("unused")
		GregorianCalendar thisday = new GregorianCalendar(TimeZone.getTimeZone("GMT"), new Locale("pt", "BR"));
		Date last = new Date();
		Date next = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try
		{
			last = df.parse(lastRunDate);
			next = df.parse(nextRunDate);
			this.last = last;
			this.next = next;
		}
		catch (ParseException pe)
		{
			System.out.println("Erro na convers�da data");
		}
		// M�do que calcula a m�a de execu� do backup
	}

	// m�do que esta comparando as datas
	public boolean execBkp()
	{
		if (next.after(last)) {
			historico = "Backup efetuado com sucesso";
		} else {
			historico = "Backup n�efetuado";
		}
		return false;
	}
}
