package ru.faulab.attendence.dto.wow;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class Guild {
    //    {"lastModified":1384694787000,"name":"Теория Хаоса","realm":"Gordunni","battlegroup":"Шквал","level":25,"side":0,"achievementPoints":2025,"members"
    public Date lastModified;
    public String name;
    public List<Member> members;

    public Guild() {
    }
}


