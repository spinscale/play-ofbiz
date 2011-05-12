//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.01 at 09:24:15 PM CEST 
//


package play.modules.ofbiz.generated.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{}attlist.condition-expr"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "condition-expr")
public class ConditionExpr {

    @XmlAttribute(name = "entity-alias")
    protected String entityAlias;
    @XmlAttribute(name = "field-name", required = true)
    protected String fieldName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String operator;
    @XmlAttribute(name = "rel-entity-alias")
    protected String relEntityAlias;
    @XmlAttribute(name = "rel-field-name")
    protected String relFieldName;
    @XmlAttribute
    protected String value;
    @XmlAttribute(name = "ignore-case")
    protected Boolean ignoreCase;

    /**
     * Gets the value of the entityAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityAlias() {
        return entityAlias;
    }

    /**
     * Sets the value of the entityAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityAlias(String value) {
        this.entityAlias = value;
    }

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldName(String value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        if (operator == null) {
            return "equals";
        } else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the relEntityAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelEntityAlias() {
        return relEntityAlias;
    }

    /**
     * Sets the value of the relEntityAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelEntityAlias(String value) {
        this.relEntityAlias = value;
    }

    /**
     * Gets the value of the relFieldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelFieldName() {
        return relFieldName;
    }

    /**
     * Sets the value of the relFieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelFieldName(String value) {
        this.relFieldName = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the ignoreCase property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getIgnoreCase() {
        if (ignoreCase == null) {
            return Boolean.FALSE;
        } else {
            return ignoreCase;
        }
    }

    /**
     * Sets the value of the ignoreCase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIgnoreCase(Boolean value) {
        this.ignoreCase = value;
    }

}
