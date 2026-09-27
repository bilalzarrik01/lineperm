package ma.youcode.lineperm.models;

public class Fichier {
    private int id;
    private String name;
    private String owner;
    private String droits;
    
    private boolean ownerRead;
    private boolean ownerWrite;
    private boolean ownerDelete;
    
    private boolean otherRead;
    private boolean otherWrite;
    private boolean otherDelete;

    public Fichier() {}

    public Fichier(int id, String name, String owner, String droits) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.droits = droits;
    }

    public Fichier(String name, String owner) {
        this.name = name;
        this.owner = owner;
        this.droits = "READ,WRITE,DELETE";
        this.ownerRead = true;
        this.ownerWrite = true;
        this.ownerDelete = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDroits() {
        return droits;
    }

    public void setDroits(String droits) {
        this.droits = droits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOwnerRead() {
        return ownerRead;
    }

    public void setOwnerRead(boolean ownerRead) {
        this.ownerRead = ownerRead;
    }

    public boolean isOwnerWrite() {
        return ownerWrite;
    }

    public void setOwnerWrite(boolean ownerWrite) {
        this.ownerWrite = ownerWrite;
    }

    public boolean isOwnerDelete() {
        return ownerDelete;
    }

    public void setOwnerDelete(boolean ownerDelete) {
        this.ownerDelete = ownerDelete;
    }

    public boolean isOtherRead() {
        return otherRead;
    }

    public void setOtherRead(boolean otherRead) {
        this.otherRead = otherRead;
    }

    public boolean isOtherWrite() {
        return otherWrite;
    }

    public void setOtherWrite(boolean otherWrite) {
        this.otherWrite = otherWrite;
    }

    public boolean isOtherDelete() {
        return otherDelete;
    }

    public void setOtherDelete(boolean otherDelete) {
        this.otherDelete = otherDelete;
    }
}
