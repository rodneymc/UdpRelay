package com.daftdroid.android.udprelay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Storage {
    private final File specDirectory;
    private final File ovpnDirectory;

    private Throwable error;

    public Storage(File dataDirectory) {

        specDirectory = new File(dataDirectory, "specs");
        specDirectory.mkdir();

        ovpnDirectory = new File(dataDirectory, "ovpn");
        ovpnDirectory.mkdir();
    }

    public boolean save(VpnSpecification spec) {

        File specfile = new File(specDirectory, Integer.toHexString(spec.getId()));
        try (
                FileOutputStream fos = new FileOutputStream(specfile);
                ObjectOutputStream out = new ObjectOutputStream((fos));
            ) {
            out.writeObject(spec);
        } catch (IOException e) {
            error = e;
            return false;
        }

        // TODO save the VPN config

        return true;
    }

    public List<VpnSpecification> loadAll() {
        List list = new ArrayList<VpnSpecification>();

        String[] filenames = specDirectory.list();

        for (String fname: filenames) {
            try {
                int id = Integer.parseInt(fname, 16);
            } catch (NumberFormatException e) {
                continue; //Ignore invalid filenames
            }

            File file = new File(specDirectory, fname);
            try (
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream in = new ObjectInputStream(fis)
                    ) {
                VpnSpecification spec = (VpnSpecification) in.readObject();
                list.add(spec);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                error = e; //TODO report me!
                continue;
            }
        }
        return list;
    }

    public void delete(VpnSpecification spec) {
        File f = new File(specDirectory, Integer.toHexString(spec.getId()));
        f.delete();
    }
}
