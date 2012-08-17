/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package edu.msu.nscl.olog;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.*;

/**
 * Logbook object that can be represented as XML/JSON in payload data.
 *
 * @author Eric Berryman taken from Ralph Lange <Ralph.Lange@bessy.de>
 */
@Entity
@Table(name = "logbooks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "is_tag", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@XmlType(propOrder = {"owner", "id", "name"})
@XmlRootElement(name = "logbook")
public class Logbook implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;
    @Column(name = "name", nullable = false, length = 250, insertable = true)
    private String name = null;
    @Column(name = "owner", nullable = false, length = 50, insertable = true)
    private String owner = null;
    @Enumerated(EnumType.STRING)
    private State state;
    //for joing the tables (many-to-many)
    @ManyToMany(mappedBy = "logbooks", fetch = FetchType.EAGER)
    private Collection<Log> logs;

    /**
     * Creates a new instance of Logbook.
     *
     */
    public Logbook() {
    }

    /**
     * Creates a new instance of Logbook.
     *
     * @param name
     * @param owner
     */
    public Logbook(String name, String owner) {
        this.owner = owner;
        this.name = name;
    }

    /**
     * Getter for logbook owner.
     *
     * @return owner logbook owner
     */
    @XmlAttribute
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for logbook owner.
     *
     * @param owner logbook owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    /**
     * Setter for tag id.
     *
     * @param id tag id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for tag name.
     *
     * @return name tag name
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * Setter for tag name.
     *
     * @param name tag name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the status
     */
    @XmlTransient
    public State getState() {
        return state;
    }

    /**
     * @return the status
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Getter for tag's Logs.
     *
     * @return logs Logs object
     */
    //@XmlElement(name = "logs")
    @XmlTransient
    public Logs getLogs() {
        if (logs != null) {
            Iterator<Log> iterator = logs.iterator();
            Logs xmlLogs = new Logs();
            while (iterator.hasNext()) {
                xmlLogs.addLog(iterator.next());
            }
            return xmlLogs;
        } else {
            return null;
        }
    }

    /**
     * Setter for tag's Logs.
     *
     * @param logs Logs object
     */
    public void setLogs(Logs logs) {
        for (Log xmlLog : logs.getLogs()) {
            this.logs.add(xmlLog);
        }
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data the Label to log
     * @return string representation for log
     */
    public static String toLogger(Logbook data) {
        return data.getName() + "(" + data.getOwner() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0
                : name.hashCode());
        result = prime * result
                + ((owner == null) ? 0 : owner.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Logbook other = (Logbook) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        return true;
    }
}
