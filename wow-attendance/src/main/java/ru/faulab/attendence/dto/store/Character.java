package ru.faulab.attendence.dto.store;

import javax.persistence.*;
import java.util.Date;

@NamedQueries({
        @NamedQuery(name = "Character.findAllActiveCharacters", query = "SELECT item FROM Character item WHERE active = true"),
        @NamedQuery(name = "Character.findCharactersByNames", query = "SELECT item FROM Character item WHERE nickname in (:nicknames)")
})
@Entity
@Table(name = "CHARACTERS")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "NICKNAME", unique = true, nullable = false)
    private String nickname;

    @Column(name = "RANG", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Rang rang;

    @Column(name = "LAST_MODIFICATION", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModification;

    @Column(name = "ACTIVE", columnDefinition = "boolean default true", nullable = false)
    private boolean active;

    protected Character() {
        active = true;
    }

    public Character(String nickname, Rang rang, Date lastModification) {
        this.nickname = nickname;
        this.rang = rang;
        this.lastModification = lastModification;
        this.active = true;
    }

    public Integer getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isActive() {
        return active;
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

    public void updateLastModification() {
        this.lastModification = new Date();
    }

    public void disable() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
