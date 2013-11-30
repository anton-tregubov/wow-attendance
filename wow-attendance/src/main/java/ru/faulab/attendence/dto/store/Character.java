package ru.faulab.attendence.dto.store;

import javax.persistence.*;
import java.util.Date;

@NamedQuery(name = "Character.findAllCharacters", query = "SELECT item FROM Character item")
@Entity
@Table(name = "CHARACTERS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"NICKNAME"})
)
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "RANG")
    @Enumerated(EnumType.ORDINAL)
    private Rang rang;

    @Column(name = "LAST_MODIFICATION")
    private Date lastModification;

    protected Character() {
    }

    public Character(String nickname, Rang rang, Date lastModification) {
        this.nickname = nickname;
        this.rang = rang;
        this.lastModification = lastModification;
    }

    public Integer getId() {
        return id;
    }

//    public void setId(Integer id) {
//        this.id = id;
//    }

    public String getNickname() {
        return nickname;
    }

//    public void setNickname(String nickname) {
//        this.nickname = nickname;
//    }

    public Rang getRang() {
        return rang;
    }

    public void setRang(Rang rang) {
        this.rang = rang;
        this.lastModification = new Date();
    }

    public Date getLastModification() {
        return lastModification;
    }

//    public void setLastModification(Date lastModification) {
//        this.lastModification = lastModification;
//    }
}
