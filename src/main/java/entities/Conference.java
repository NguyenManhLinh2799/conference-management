package entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "conference")
public class Conference {
    @Id
    @Column(name = "idConference")
    @GeneratedValue(generator = "incrementator")
    @GenericGenerator(name = "incrementator", strategy = "increment")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "summary")
    private String summary;

    @Column(name = "detail")
    private String detail;

    @Column(name = "image")
    private String image;

    @Column(name = "time")
    private Date time;

    @Column(name = "number_of_attendees")
    private int numberOfAttendees;

    @OneToMany(mappedBy = "conference")
    private List<RegisterConference> registerConferences;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idLocation")
    private Location location;

    public Conference() {
    }

    public Conference(String name, String summary, String detail, String image, Date time, Location location, int numberOfAttendees) {
        this.name = name;
        this.summary = summary;
        this.detail = detail;
        this.image = image;
        this.time = time;
        this.location = location;
        this.numberOfAttendees = numberOfAttendees;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getTime() {
        return time;
    }

    public String getDateOnly() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(time);
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Location getLocation() { return location; }

    public String getLocationName() { return location.getName(); }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getNumberOfAttendees() {
        return numberOfAttendees;
    }

    public void setNumberOfAttendees(int numberOfAttendees) {
        this.numberOfAttendees = numberOfAttendees;
    }
}
