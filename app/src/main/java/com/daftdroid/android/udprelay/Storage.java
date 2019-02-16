package com.daftdroid.android.udprelay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    File[] files = specDirectory.listFiles();

        for (File file: files) {
            try {
                Long.parseLong(file.getName(), 16);
            } catch (NumberFormatException e) {
                continue; //Ignore invalid filenames
            }

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
    public VpnSpecification load(int id) {
        String fname = Integer.toHexString(id);

        File file = new File(specDirectory, fname);
        try (
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fis)
        ) {
            VpnSpecification spec = (VpnSpecification) in.readObject();
            return spec;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            error = e; //TODO report me!
            return null;
        }
    }

    public void delete(VpnSpecification spec) {
        File f = new File(specDirectory, Integer.toHexString(spec.getId()));
        f.delete();
    }

    // Pick a random int id that represents a file that doesn't exist, and not zero.
    public int getNewSpecId() {
        Random random = new Random();

        while (true) {
            int id = random.nextInt();
            if (id != 0 && ! new File(specDirectory, Integer.toHexString(id)).exists()) {
                return id;
            }
        }
    }
}
