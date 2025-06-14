package com.intercorp.ms05.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class Root {

    private Person person;
    private int random;

    @XmlElement(name = "random_float")
    private double randomFloat;

    private boolean bool;
    private String date;
    private String regEx;

    @XmlElement(name = "enum")
    private String enumValue;

    @XmlElement(name = "elt")
    private List<String> elements;

    @XmlTransient
    private int age; // lo asignaremos manualmente fuera del XML
}
