package entities;

import javax.persistence.*;

@Entity
@Table(name = "register_conference")
@IdClass(RegConfId.class)
public class RegisterConference {
    @Id
    @Column(name = "idUser")
    private int idUser;

    @Id
    @Column(name = "idConference")
    private int idConference;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "idUser")
    @MapsId
    private User user;

    @ManyToOne
    @JoinColumn(name = "idConference")
    @MapsId
    private Conference conference;

    public RegisterConference() {
    }

    public RegisterConference(int idUser, int idConference) {
        this.idUser = idUser;
        this.idConference = idConference;
        this.status = "Đang chờ";
    }

    public int getIdUser() {
        return idUser;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() { return user.getUsername(); }

    public String getFullname() { return user.getFullname(); }

    public String getEmail() { return user.getEmail(); }



    public int getIdConference() {
        return idConference;
    }

    public Conference getConference() {
        return conference;
    }

    public String getNameConference() {
        return conference.getName();
    }

    public String getSummaryConference() {
        return conference.getSummary();
    }

    public String getLocationConference() {
        return conference.getLocation().getName();
    }

    public String getTimeConference() {
        return conference.getDateOnly();
    }

    public int getNumberOfCandidates() { return conference.getNumberOfAttendees(); }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
