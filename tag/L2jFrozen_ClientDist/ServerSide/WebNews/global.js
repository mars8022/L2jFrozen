
function showFlashObject(objID, objSource, objWidth, objHeight, objQuality, objWmode, objBgcolor)
{
	//page url
	var pageUrl = self.window.location.href;
	if(pageUrl.substring(0,5) == 'https') {
		swfUrl = "https";
	} else {
		swfUrl = "http";
	}
	/* Default Value Setting */
	if (objID == "") objID = 'ShockwaveFlash1';
	if (objWidth == "") objWidth = '0';
	if (objHeight == "") objHeight = '0';
	if (objQuality == "") objQuality = 'best';
	if (objWmode == "") {
		objWmode = 'transparent';
	} else {
		objWmode = '';
	}
	/* Flash 8.0 version */
	document.write('<OBJECT id="' + objID + '" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"');
	document.write('type="application/x-shockwave-flash" ');
	document.write('data="' + objSource + '" ');
	document.write('codebase="' + swfUrl +'://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab" ');
	document.write('WIDTH="' + objWidth + '" HEIGHT="' + objHeight + '">');
	document.write('<PARAM NAME=menu value=false>');
	document.write('<PARAM NAME=allowScriptAccess value=always>');
	document.write('<param name=scale value=exactfit>');
	document.write('<PARAM NAME=wmode value="'+ objWmode +'">');
	document.write('<PARAM NAME=movie VALUE="'+ objSource +'">');
	document.write('<PARAM NAME=quality VALUE="'+ objQuality +'">');
	document.write('<PARAM NAME=bgcolor VALUE="'+ objBgcolor +'">');
	document.write('<embed allowScriptAccess="always" swLiveConnect="true" src="' + objSource + '" menu="false" quality="' + objQuality + '" wmode="' + objWmode + '" bgcolor="' + objBgcolor + '" width="' + objWidth + '" height="' + objHeight + '" type="application/x-shockwave-flash" pluginspace="http://www.macromedia.com/go/getflashplayer">');
	document.write('</OBJECT>');
}

