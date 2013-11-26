package ru.faulab.attendence.store;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@NamedQuery(name = "Attendance.findAllAttendancesInPeriod",query = "SELECT item FROM Attendance item where item.key.day between :fromDate and :toDate")
@Entity
@Table(name = "ATTENDANCES")
public class Attendance {

    @ManyToOne
    @MapsId("characterId")
    private Character character;

    @EmbeddedId
    private Key key;

    public Attendance() {
    }

    public Attendance(Character character, Date date) {
        this.character = character;
        this.key = new Key(character.getId(), date);
    }

    public Character getCharacter() {
        return character;
    }

    public Date getDay() {
        return key.getDay();
    }

    public Key getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attendance that = (Attendance) o;

        if (!character.equals(that.character)) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = character.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Embeddable
    public static class Key implements Serializable {
        @Column(name = "CHARACTER_ID")
        private Integer characterId;
        @Column(name = "day")
        @Temporal(TemporalType.DATE)
        private Date day;

        public Key() {
        }

        public Key(Integer characterId, Date day) {
            this.characterId = characterId;
            this.day = day;
        }

        public Integer getCharacterId() {
            return characterId;
        }

        public Date getDay() {
            return day;
        }

        @Override
        public int hashCode() {
            return characterId.hashCode() + (day.hashCode() << 1);
        }

        @Override
        public boolean equals(Object that) {
            return (this == that) || ((that instanceof Key) && this.characterId.equals(((Key) that).characterId) && this.day.equals(((Key) that).day));
        }

    }
}