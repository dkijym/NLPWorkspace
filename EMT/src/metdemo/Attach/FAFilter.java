package metdemo.Attach;
import java.io.File;
//import javax.swing.*;
import javax.swing.filechooser.*;

public class FAFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String ext = null;
	String tmps = f.getName();
        int i = tmps.lastIndexOf('.');
        if (i > 0 &&  i < tmps.length() - 1) {
            ext = tmps.substring(i+1).toLowerCase();
        }
	if(ext != null) {
            if(ext.equals("fa"))
            {
                return true;
            }
		return false;
	}

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "File Analyzer Models";
    }
}
