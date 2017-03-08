## Database Standards

The following guide covers the best practices for database structures within this application.

## Table of Contents
* [Vendor Agnostic](#vendor-agnostic)
* [Tables](#tables)
* [Columns](#columns)
* [Indexes](#indexes)
* [Logical Deletes](#logical-deletes)

## Vendor Agnostic
Since any backend database could potentially be used with this API, it is strongly encouraged to follow [ANSI Standard SQL 1999](https://en.wikipedia.org/wiki/SQL:1999), which is supported by nearly all of the major database vendors.

__Rules__
* Use common data types when possible
* SQL must conform to [SQL99](https://en.wikipedia.org/wiki/SQL:1999) standards (do not use vendor specific syntax of any kind)
* Avoid use of: stored procedures, functions, trigger, or views.
* Test all SQL / DDL against SQL Server Express, Oracle XE, and MySQL
* Be consistent with naming of objects! (If you prefer PascalCase over underscores, then do this consistently)

:arrow_up: [Back to Top](#table-of-contents)

## Tables

### Naming
* Use underscores between words in database objects since Hibernate defaults to this naming method
  - DO: user_role
  - DON'T: UserRole
* Table names should use the singular form.
  - A table that stores user records will be called 'user'.
  - In the case of a join table, such as a table that stores the relationship between a role and permissions, use the singular form: role_permission.
* When modeling a hierarchy, the parent of the relationship should always come first. For example a user has many phone numbers. The table then should be called user_phone.
* Keep object names 30 characters or less (primarily to support Oracle databases)

### Primary Keys
Every table should have a primary key. Typically this will be [surogate key](https://en.wikipedia.org/wiki/Surrogate_key) implemented as an identity column. Using an integer primary key value fits well with the web service pattern of being able to reference a particular record within an entity set with a single value. A multipart natural key will not work very well when given within a REST URL, which is one of many reason why they are not used throughout the API.

These keys should use the following naming convention:

```
PK_{table_name}

PK_action_log

```

### Foreign Keys
Referential integrity should be enforced via foreign key relationships. These keys should use the following naming convention:

```
FK_{Foreign_key_table}_{primary_key_table}_{primary_key_column}

FK_role_permission_roleId

```

:arrow_up: [Back to Top](#table-of-contents)

## Columns

### Naming
1. Column names should be descriptive
2. Column names should avoid abbreviations
3. Columns in different tables that represent the same type of data should be named the same.
   - For example, a column that holds a zip code should be called zip_code in all tables.
4. Columns referencing a foreign key should be labeled {table_name}_id
   - For example: client_id FK to client.id column
5. Do NOT:
   - put the datatype in the name of the column (if a datatype lookup is needed, then the user can query the database)
   - prefix the column name with the object type of "col_" as this is redundant

### Data Types
Columns should try to adhere to a standard set of data types and column sizes based on their content.

|Primitive Type | Column Data Type | Additional Information
|:----|:----|:----|
|String | nvarchar | Enforce a maximum length when the possible set of values is well known|
| Boolean | bit | Hibernate maps bit columns into a true/false boolean. Avoid using char(1) or other textual representations of bool values due to inconsistenancy in input as well as lack of meaning in other languages.|
| DateTime | datetime2 / datetime / time / datetimeoffset | Choose the appropriate datat type depending on the type of value needs to be stored (Does the value have a time component?).|
| Integer | int | 

For a complete list [see](https://msdn.microsoft.com/en-us/library/cc716729(v=vs.110).aspx)

If the column size is known and exact, then consider allocating only the space needed for the data of the column. If the size of the column is unknown, then approximate the maximum value that could possibly be used or supported and round up to a common column size. 

Example column size standards:
|Type|Sizes|
|nvarchar|250, 500, 2000, text, clob|
|numeric|(18,0)|
|numeric money|(18,2)|

##### General Recommendations
1. Use a date or time column to store temporal information.
  1. An nvarchar or char column **should not be used to store temporal information.**
2. Store all time in UTC.
3. Use bit fields to store boolean values.
  1. An nvarchar or char column **should not be used to store boolean values.**
    1. Using characters to represent boolean values can lead to inconsistent values being entered to represent true and false. For example: Y, N, y, n, T, t, F, f, Y, y, N, n
    2. Using characters to represent boolean loses any implicit meaning outside of the English language. (Oui, Si)

### Operational Columns
Operational columns will be added to every table. The standard columns are:

| Column Name | Data Type | Nullable | Description
| ------------- |:-------------:| :-----:| :-----|
| created_date | DateTime | No | Row create date|
| created_by | nvarchar(255) | No | Create user |
| last_modified_date | DateTime | No | Row modify date |
| last_modified_by | nvarchar(255) | No | Modify user |
| is_deleted  | bit | No | Indicates if the record has been logically deleted|

:arrow_up: [Back to Top](#table-of-contents)

## Indexes
1. Create a clustered index on the primary key  (default behavior)
2. When queries are created within the app, it is the responsibility of the developer to examine the table structure to determine additional indexes that will optimize performance
3. Configure the database to log slow running queries and notify developers when a query hits the maximum allowable response time


:arrow_up: [Back to Top](#table-of-contents)

## Logical Deletes
The application will follow a soft delete pattern. Every table will have an operational column defined that signifies whether or not the record has been deleted. When a delete is requested, this column value will be set to indicate the record has been deleted.

```
is_deleted = true/false
```

Logically deleting a record from the database ensures that ALL data is retained for auditing purposes. An added benefit to logical deletes is that if data is deleted in the app by mistake, then a database administrator is able to update the is_deleted column back to 'false'. 

NOTE: Application developers must be mindful to only fetch records where is_deleted = false, otherwise users will see 'deleted' data. 

### Cascading Logical Deletes
> __VERIFIY IMPLEMENTATION__

#### One-to-Many - Parent Owns Childrens
Logical deletes should cascade down to child tables in one-to-many relationships where the parent _owns_ the children, which should be most cases. This is not an absolute rule, but consider adding an _is_deleted_ column to a one-to-many table and updating the value when the parent record is deleted or restored.

__Example:__ When the User record for "John Smith" is logically deleted, then the associated phone records shoudld be logically deleted as well.
```
User
- name = John Smith
- is_deleted = true
 |--- user_phone
      - phone_number = 123-123-1233
      - is_deleted = true
```

#### Many-to-Many - Parent Doesn't Exclusively Own Children
When a parent doesn't exclusively own the children it is associated with, then do not cascade logical deletes. Each situation might be different, so this is not an absolute rule. Use good judgement to determine what is necessary. When in doubt, don't cascade deletes.

__Example:__ An Organization has many Users and a User can be associated with many Organizations. Logical deletes should not be cascaded in either direction since they each are independent entities.
```
Organization
    |----- OrganizationUser
                  |----- User
```

:arrow_up: [Back to Top](#table-of-contents)
