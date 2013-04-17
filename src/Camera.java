import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.Image;
import com.lti.civil.awt.AWTImageConverter;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Camera extends JApplet implements CaptureObserver {

	private static final long serialVersionUID = 1L;
	CaptureStream captureStream = null;
	private byte[] bytes;
	JLabel lb=new JLabel();
	Boolean isOld=false;

	public void init() {
		CaptureSystemFactory factory = DefaultCaptureSystemFactorySingleton
				.instance();
		CaptureSystem system;
		add(lb, BorderLayout.CENTER);
		try {
			system = factory.createCaptureSystem();
			system.init();
			List list = system.getCaptureDeviceInfoList();
			int i = 0;
			if (i < list.size()) {
				CaptureDeviceInfo info = (CaptureDeviceInfo) list.get(i);
				captureStream = system.openCaptureDeviceStream(info
						.getDeviceID());
				captureStream.setObserver(Camera.this);
			}
		} catch (CaptureException ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		setSize(800, 600);
		try {
			captureStream.start();
		} catch (CaptureException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void stop(){
		isOld=false;
		try {
			captureStream.stop();
		} catch (CaptureException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void destroy() {
		try {
			captureStream.dispose();
		} catch (CaptureException e) {
		}
	}

	@Override
	public void onError(CaptureStream arg0, CaptureException arg1) {
	}

	@Override
	public void onNewImage(CaptureStream stream, Image image) {
		if (image == null) {
			bytes = null;
			return;
		} else if(isOld)
			return; //after the first capture don't take a new image
		isOld=true;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(os);
			jpeg.encode(AWTImageConverter.toBufferedImage(image));
			os.close();
			bytes = os.toByteArray();
			String fileDir = "C:/temp/kostya123.jpg";
			File file = new File(fileDir);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
			BufferedImage myImage = ImageIO.read(file);
			// capture only the first image
				lb.setIcon(new ImageIcon(myImage));
				lb.setOpaque(true);
				repaint();

		} catch (IOException e) {
			System.out.println(e.getMessage());
			bytes = null;
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			bytes = null;
		}

	}

}
