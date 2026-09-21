package ma.youcode.lineperm.models;

public class Fichier {
    private String name;
    private String owner;
    
    private boolean ownerRead;
    private boolean ownerWrite;
    private boolean ownerDelete;
    
    private boolean otherRead;
    private boolean otherWrite;
    private boolean otherDelete;

    public Fichier() {}

    public Fichier(String name, String owner) {
        this.name = name;
        this.owner = owner;
        this.ownerRead = true;
        this.ownerWrite = true;
        this.ownerDelete = true;
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