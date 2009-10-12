package dd.util;

/** This code is from 
 
http://xmlgraphics.apache.org/batik/using/transcoder.html
*/

import java.io.*;

import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class SaveAsJPEG {

    public static void main(String[] args) throws Exception {

	if (args.length != 2) {
	    System.out.println("Usage: java dd.util.SaveAsJPEG input.svg out.jpg");
	    System.exit(1);
	}
	

        // Create a JPEG transcoder
        JPEGTranscoder t = new JPEGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
                             new Float(.8));

        // Create the transcoder input.
        //String svgURI = new File(args[0]).toURL().toString();
        String svgURI = new File(args[0]).toURI().toString();
        TranscoderInput input = new TranscoderInput(svgURI);

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream(args[1]);
        TranscoderOutput output = new TranscoderOutput(ostream);

	//t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(600));
	//t.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(600));
        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
        System.exit(0);
    }
}
