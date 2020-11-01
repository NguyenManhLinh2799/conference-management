package entities;

import java.io.Serializable;

public class RegConfId implements Serializable {
    private int idUser;
    private int idConference;

    public RegConfId() {
    }

    public RegConfId(int idUser, int idConference) {
        this.idUser = idUser;
        this.idConference = idConference;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
