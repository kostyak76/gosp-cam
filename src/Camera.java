//TODO: in Java, we should put all the classes to packages, resembling a reversed domaing name. See below for examples.
// package ua.kostyak.cameraapplet
import com.lti.civil.*;
import com.lti.civil.Image;
import com.lti.civil.awt.AWTImageConverter;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
            //TODO: really strange construct; no loop here. Checking if there is at least 1 camera may be written like
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
            // TODO: in applets, this goes to nowhere. Show a warning to user with message box, or draw that text instead
            // of a captured pic, preferably with instructions on how to fix (connect a camera, call support, etc).
            ex.printStackTrace();
        }
    }

    //TODO: in java 5, there was a specicic annotation added to the lang @Override.
    //http://stackoverflow.com/questions/94361/when-do-you-use-javas-override-annotation-and-why
    @Override
    public void start()
    {
        setSize(800, 600);
        try
        {
            // TODO: possible NullPointerException. We should check state first (for us, the captureStream should not be
            // null which means that connection to the camera is successfull.
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
        if (image == null)
        {
            bytes = null;
            return;
            // TOOD: else below is not needed. Return on top is enough. Though, is this a normal state when we receive
            // an image data of null? if this is an eerror, we may try logging it or showing to user somehow.
        } else if (isOld)
            return; //after the first capture don't take a new image
        isOld = true;
        try
        {
            //TODO output stream opened and may be not closed. Streams and other vital resources should be closed explicitly
            // with the http://www.baptiste-wicht.com/2010/08/java-7-try-with-resources-statement/ try-with resource
            // or wih even more explicit try-finally block (google it).
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

        } catch (IOException e)
        {
            System.out.println(e.getMessage());
            bytes = null;
        } catch (Throwable t)
        {
            System.out.println(t.getMessage());
            bytes = null;
        }

    }

}
