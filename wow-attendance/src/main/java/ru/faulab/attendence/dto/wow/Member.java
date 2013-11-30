package ru.faulab.attendence.dto.wow;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Member {
    public Hero character;
    public int rank;
}
