package ua.kostyak.cameraapplet;

import com.lti.civil.*;
import com.lti.civil.Image;
import com.lti.civil.awt.AWTImageConverter;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Camera extends JApplet implements CaptureObserver
{

	private static final long serialVersionUID = 1L;
	CaptureStream captureStream = null;
	JLabel lb = new JLabel();
	Boolean isOld = false;
	private byte[] bytes;

	public void init()
	{
		CaptureSystemFactory factory = DefaultCaptureSystemFactorySingleton
				.instance();
		CaptureSystem system;
		add(lb, BorderLayout.CENTER);
		try
		{
			system = factory.createCaptureSystem();
			system.init();
			List list = system.getCaptureDeviceInfoList();
			// TODO: really strange construct; no loop here. Checking if there
			// is at least 1 camera may be written like
			// TODO: also we must to perform situation if camera is not exists
			// TODO: change for two cameras and if it is not an scaner
			// if(list.size()>0) { .. list.get(0); } -> hence no need for i var.
			int i = 0;
			if (i < list.size())
			{
				CaptureDeviceInfo info = (CaptureDeviceInfo) list.get(i);
				captureStream = system.openCaptureDeviceStream(info
						.getDeviceID());
				captureStream.setObserver(Camera.this);
			}
		} catch (CaptureException ex)
		{
			// TODO: in applets, this goes to nowhere. Show a warning to user
			// with message box, or draw that text instead
			// of a captured pic, preferably with instructions on how to fix
			// (connect a camera, call support, etc).
			ex.printStackTrace();
		}
	}

	// TODO: in java 5, there was a specicic annotation added to the lang
	// @Override.
	// http://stackoverflow.com/questions/94361/when-do-you-use-javas-override-annotation-and-why
	@Override
	public void start()
	{
		setSize(800, 600);
		try
		{
			// TODO: possible NullPointerException. We should check state first
			// (for us, the captureStream should not be
			// null which means that connection to the camera is successfull.
			if (captureStream != null)
				captureStream.start();
		} catch (CaptureException e)
		{
			// TODO: same as above;
			System.out.println(e.getMessage());
		}
	}

	public void stop()
	{
		isOld = false;
		try
		{
			if (captureStream != null)
				captureStream.stop();
		} catch (CaptureException e)
		{
			System.out.println(e.getMessage());
		}
	}

	// tis allright
	public void destroy()
	{
		try
		{
			if (captureStream != null)
				captureStream.dispose();
		} catch (CaptureException e)
		{
		}
	}

	@Override
	public void onError(CaptureStream arg0, CaptureException arg1)
	{
	}

	@Override
	public void onNewImage(CaptureStream stream, Image image)
	{
		if (image == null || isOld)
		{
			bytes = null;
			return;
			// TODO: else below is not needed. Return on top is enough. Though,
			// is this a normal state when we receive
			// an image data of null? if this is an eerror, we may try logging
			// it or showing to user somehow.
		}
		isOld = true;
		ByteArrayOutputStream os = null;
		FileOutputStream fos = null;
		try
		{
			// TODO output stream opened and may be not closed. Streams and
			// other vital resources should be closed explicitly
			// with the
			// http://www.baptiste-wicht.com/2010/08/java-7-try-with-resources-statement/
			// try-with resource
			// or wih even more explicit try-finally block (google it).
			os = new ByteArrayOutputStream();
			JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(os);
			jpeg.encode(AWTImageConverter.toBufferedImage(image));

			bytes = os.toByteArray();
			
			//upload image to the server
			String charset = "UTF-8";
			String requestURL = "http://www.mysite.ks/index.php/files_upload/upload";
			MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            multipart.addFilePartFromBuffImage("image1", "kostya123.jpg", bytes);
            
            List<String> response = multipart.finish();
			// capture only the first image
            for (String line : response) {
            	lb.setText(line);
            }
			lb.setOpaque(true);
			repaint();

		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			bytes = null;
		} catch (Throwable t)
		{
			System.out.println(t.getMessage());
			bytes = null;
		} finally
		{
			try
			{
				if (os != null)
					os.close();
				if (fos != null)
					fos.close();
			} catch (IOException e)
			{
			}
		}

	}

}
