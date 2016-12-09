package com.lighthousesoftware.launchpad.common.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.lighthousesoftware.launchpad.common.util.DateTimeUtil
import org.grails.datastore.gorm.GormEntity

@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
abstract class BaseEntity<D> implements GormEntity<D> {

    Long version
    Date createdOn
    Date updatedOn
    Long createdBy
    Long updatedBy

    static constraints = {
        version display:false
        createdOn display:false
        updatedOn display:false
        createdBy display:false
        updatedBy display:false
    }

    static transients = [ "dynamicProps" ]

    static mapping = {
        id generator: 'identity'
        sort updatedOn:'desc'
    }

    @JsonIgnore def dynamicProps = [:]
    def propertyMissing(String name, value) { dynamicProps[name] = value }
    def propertyMissing(String name) { dynamicProps[name] }

    def beforeInsert() {
        initDefaults()
        return true
    }

    def beforeUpdate() {
        updatedBy = 0
        updatedOn = DateTimeUtil.now()
    }

    def beforeValidate() {
        // this is called after : beforeSave, beforeUpdate & on entity.save, entity.validate
        initDefaults()
        return true
    }


    public initDefaults() {
        if (createdBy == null) {
            createdBy = 0
        }
        if (createdOn == null) {
            createdOn = DateTimeUtil.now()
        }
        if (updatedBy == null) {
            updatedBy = 0
        }
        if (updatedOn == null) {
            updatedOn = DateTimeUtil.now()
        }
    }
}


