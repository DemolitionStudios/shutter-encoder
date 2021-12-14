/*******************************************************************************************
* Copyright (C) 2021 PACIFICO PAUL
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
* 
********************************************************************************************/

package functions;

import java.io.File;

import javax.swing.JOptionPane;

import application.Ftp;
import application.Settings;
import application.Shutter;
import application.Utils;
import application.Wetransfer;
import library.FFMPEG;
import library.FFPROBE;
import settings.FunctionUtils;

public class AudioNormalization extends Shutter {
	
	private static int audioTracks;
	
	public static void main() {
		
		Thread thread = new Thread(new Runnable(){			
			@Override
			public void run() {
				
				if (scanIsRunning == false)
					FunctionUtils.completed = 0;
				
				lblFilesEnded.setText(FunctionUtils.completedFiles(FunctionUtils.completed));

				for (int i = 0 ; i < liste.getSize() ; i++)
				{
					File file = FunctionUtils.setInputFile(new File(liste.getElementAt(i)));		
					
					if (file == null)
						break;
		            
					try {
						
						String fileName = file.getName();
						String extension =  fileName.substring(fileName.lastIndexOf("."));
						
						lblCurrentEncoding.setText(Shutter.language.getProperty("analyzing") + " " + fileName);	
						
						//Data analyze
						if (FunctionUtils.analyze(file, false) == false)
							continue;
						
		            	//filterComplex
						String filterComplex = setFilterComplex();	
						
						//Output folder
						String labelOutput = FunctionUtils.setOutputDestination("", file);	
	
						//File output name
						String extensionName = "_Norm";
						
						if (Settings.btnExtension.isSelected())
							extensionName = Settings.txtExtension.getText();
						
						//Output name
						String fileOutputName =  labelOutput.replace("\\", "/") + "/" + fileName.replace(extension, extensionName + extension); 
						
						//Audio
						String audio = setAudio(extension);
						
						//File output
						File fileOut = new File(fileOutputName);				
						if (fileOut.exists())		
						{						
							fileOut = FunctionUtils.fileReplacement(labelOutput, fileName, extension, extensionName + "_", extension);
							
							if (fileOut == null)
								continue;						
						}
															
						//Command
						String cmd;
						if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
							cmd =  " -vn" + filterComplex + " -f null -";					
						else
							cmd =  " -vn" + filterComplex + " -f null -" + '"';	
						
						FFMPEG.run(" -i " + '"' + file.toString() + '"' + cmd);		
						
						do
						{
							Thread.sleep(100);
						}
						while(FFMPEG.runProcess.isAlive());
						
						lblCurrentEncoding.setText(fileName);	
						
						if (cancelled == false)
						{
							//Command
							if (FFPROBE.stereo)
							{
								cmd = " -filter_complex volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB -c:v copy -c:s copy" + audio + " -y ";
							}
						    else if (FFPROBE.channels > 1)	
						    {
						    	if (FFPROBE.channels >= 4)	    		
						    	{
									if (audioTracks == 0)
										cmd = " -filter_complex " + '"' + "[0:a:0]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a1];[0:a:1]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a2]" + '"' + " -c:v copy -c:s copy" + audio.replace("-map a?", "-map [a1] -map [a2] -map 0:a:2? -map 0:a:3? -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7?") + " -y ";
							    	else
							    		cmd = " -filter_complex " + '"' + "[0:a:2]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a3];[0:a:3]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a4]" + '"' + " -c:v copy -c:s copy" + audio.replace("-map a?", "-map 0:a:0 -map 0:a:1 -map [a3] -map [a4] -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7?") + " -y ";
						    	}
						    	else
						    		cmd = " -filter_complex " + '"' + "[0:a:0]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a1];[0:a:1]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a2]" + '"' + " -c:v copy -c:s copy" + audio.replace("-map a?", "-map [a1] -map [a2] -map 0:a:2? -map 0:a:3? -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7?") + " -y ";
						    }				
	
							FFMPEG.run(" -i " + '"' + file.toString() + '"' + cmd + '"' + fileOut + '"');							
							
							do
							{
								Thread.sleep(100);
							}
							while(FFMPEG.runProcess.isAlive());
							
							if (FFMPEG.error)
							{
								if (FFPROBE.stereo)
									cmd = " -filter_complex volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB -c:v copy -c:s copy -c:a aac -ar " + lbl48k.getText() + " -b:a 320k -map v:0? -map a? -map s? -y ";
							    else if (FFPROBE.channels > 1)	
							    {
							    	if (FFPROBE.channels >= 4)	    		
							    	{
										if (audioTracks == 0)
											cmd = " -filter_complex " + '"' + "[0:a:0]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a1];[0:a:1]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a2]" + '"' + " -c:v copy -c:s copy -c:a aac -ar " + lbl48k.getText() + " -b:a 320k -map v:0? -map [a1] -map [a2] -map 0:a:2? -map 0:a:3? -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7? -map s? -y ";
								    	else
								    		cmd = " -filter_complex " + '"' + "[0:a:2]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a3];[0:a:3]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a4]" + '"' + " -c:v copy -c:s copy -c:a aac -ar " + lbl48k.getText() + " -b:a 320k -map v:0? -map 0:a:0 -map 0:a:1 -map [a3] -map [a4] -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7? -map s? -y ";
							    	}
							    	else
							    		cmd = " -filter_complex " + '"' + "[0:a:0]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a1];[0:a:1]volume=" + String.valueOf(FFMPEG.newVolume).replace(",", ".") + "dB[a2]" + '"' + " -c:v copy -c:s copy -c:a aac -ar " + lbl48k.getText() + " -b:a 320k -map v:0? -map [a1] -map [a2] -map 0:a:2? -map 0:a:3? -map 0:a:4? -map 0:a:5? -map 0:a:6? -map 0:a:7? -map s? -y ";
							    }	
								
								FFMPEG.run(" -i " + '"' + file.toString() + '"' + cmd + '"' + fileOut + '"');	
							}
							
							do
							{
								Thread.sleep(100);
							}
							while(FFMPEG.runProcess.isAlive());
						}
	
						if (FFMPEG.saveCode == false && btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false)
						{
							if (lastActions(fileName, fileOut, labelOutput))
							break;
						}
						
					} catch (InterruptedException e) {
						FFMPEG.error  = true;
					}
				}	
				
				if (btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false)
					enfOfFunction();
			}
			
		});
		thread.start();
		
    }

	private static String setFilterComplex() {
	
		if (FFPROBE.stereo)
			return " -filter_complex ebur128=peak=true";
	    else if (FFPROBE.channels > 1)	
	    {
	    	if (FFPROBE.channels >= 4)	    		
	    	{
	    		String[] options = {"A1 & A2", "A3 & A4"};
	    		audioTracks = JOptionPane.showOptionDialog(frame, language.getProperty("ChooseMultitrack"), language.getProperty("multitrack"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	    		if (audioTracks == 0)
		    		return " -filter_complex " + '"' + "[0:a:0][0:a:1]amerge=inputs=2[a];[a]ebur128=peak=true" + '"';
		    	else
		    		return " -filter_complex " + '"' + "[0:a:2][0:a:3]amerge=inputs=2[a];[a]ebur128=peak=true" + '"';
	    	}
	    	else
	    		return " -filter_complex " + '"' + "[0:a:0][0:a:1]amerge=inputs=2[a];[a]ebur128=peak=true" + '"';
	    }
	    else
	    	return " -filter_complex ebur128=peak=true";
	}

	private static String setAudio(String ext) {		
		
		if (caseChangeAudioCodec.isSelected())
		{
			if (comboAudioCodec.getSelectedItem().toString().contains("PCM"))
			{
				switch (comboAudioCodec.getSelectedIndex()) 
				{
					case 0 :
						return " -c:a pcm_f32le -ar " + lbl48k.getText() + " -b:a 1536k -map v:0? -map a? -map s?";
					case 1 :
						return " -c:a pcm_s32le -ar " + lbl48k.getText() + " -b:a 1536k -map v:0? -map a? -map s?";
					case 2 :
						return " -c:a pcm_s24le -ar " + lbl48k.getText() + " -b:a 1536k -map v:0? -map a? -map s?";
					case 3 :
						return " -c:a pcm_s16le -ar " + lbl48k.getText() + " -b:a 1536k -map v:0? -map a? -map s?";
				}
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("AAC"))
			{
				return " -c:a aac -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k -map v:0? -map a? -map s?";
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("MP3"))
			{
				return " -c:a libmp3lame -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k -map v:0? -map a? -map s?";
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("AC3"))
			{
				return " -c:a ac3 -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k -map v:0? -map a? -map s?";
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("OPUS"))
			{
				return " -c:a libopus -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k";
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("OGG"))
			{
				return " -c:a libvorbis -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k";
			}
			else if (comboAudioCodec.getSelectedItem().toString().equals("Dolby Digital Plus"))
			{
				return " -c:a eac3 -ar " + lbl48k.getText() + " -b:a " + comboAudioBitrate.getSelectedItem().toString() + "k";
			}
		}
		else //Mode Auto
		{
			switch (ext.toLowerCase()) {
				case ".mp4":
					return " -c:a aac -ar " + lbl48k.getText() + " -b:a 256k -map v:0? -map a? -map s?";
				case ".mp3":
					return " -c:a mp3 -ar " + lbl48k.getText() + " -b:a 256k -map v:0? -map a? -map s?";
				case ".wmv":
					return " -c:a wmav2 -ar " + lbl48k.getText() + " -b:a 256k -map v:0? -map a? -map s?";
				case ".mpg":
					return " -c:a mp2 -ar " + lbl48k.getText() + " -b:a 256k -map v:0? -map a? -map s?";
				case ".ogv":
				case ".av1":
				case ".webm":
					return " -c:a libopus -ar " + lbl48k.getText() + " -b:a 192k -map v:0? -map a? -map s?";
			}
		}
		
		if (FFPROBE.qantization == 24)
			return " -c:a pcm_s24le -map v:0? -map a? -map s?";
		else if (FFPROBE.qantization == 32)
			return " -c:a pcm_s32le -map v:0? -map a? -map s?";
		else
			return " -c:a pcm_s16le -map v:0? -map a? -map s?";
	}
	
	private static boolean lastActions(String fileName, File fileOut, String output) {
		
		if (FunctionUtils.cleanFunction(fileName, fileOut, output))
			return true;
		
		//Sending processes
		Utils.sendMail(fileName);
		Wetransfer.addFile(fileOut);
		Ftp.sendToFtp(fileOut);
		Utils.copyFile(fileOut);
		
		
		//Watch folder
		if (Shutter.scanIsRunning)
		{
			FunctionUtils.moveScannedFiles(fileName);
			AudioNormalization.main();
			return true;
		}
		return false;
	}
	
}
