# BASIC-ORM
Beta Android database helper using Annotation processor and Sqlite database.

Orm does not behave like 'traditional' orm libraries. Entities are not binned to any "session" or something like this.
Entities are simple java objects and can behave exactly like you expect to.
 
Manage CRUD operation as simple as you can and leave Basic-orm generate for you all the boring and error prone code.

It also provide some db optimisations like using prepared statements, caching, Hibernate Criteria like querying, and version control.

## Usage

Annotate your class with `@Entity` than define class properties with `@Column`. 
Orm handles query, insert, update and delete and schema generation.

###Entity example :
```
@Entity
public class Castle {
    
    @Id
    long id;
    
    @Column
    String name;
    
    // required by basic orm
    Castle(){}
    
    public Castle(String name){
        this.name = name;
    }
       
    ... getters & setters ... 
}
```

Declare in your `AndroidManifest.xml` file the database name :
```
...
   <application>
        ...
        <meta-data android:name="orm.dbName" android:value="{name}"/>
        ....
    </application>
...
```

Get orm instance :
```
Orm.getInstance(context);
```

### CRUD operation

```
Orm orm = Orm.getInstance(context);

// Create a new castle.
Castle castle = new Castle("Camelot");
long id = orm.insertInTx(Castle.class, foo);

// Load from db 
castle = orm.loadById(Castle.class, id)

// Update the castle's name.
castle.setName("Of AAAARRrrrrghhhhhh");
orm.updateInTx(Castle.class, foo);

// Delete (not a suitable place).
orm.deleteInTx(Castle.class, castle);

```

## Basic Rules

Fields type must be one of : 
* Primitives  (`boolean`, `long`, `double`, `int`, `float`, `short`, `byte`, `char`, `short`)
* Primitives wrapper (`Integer`, `Boolean`...)
* `String` or `java.util.Date`
* `byte[]`

Field visibility must be `public` or `default` (package protected).

Class must provide a `public` or `default` (package protected) no-arguments constructor.

Entity must have a unique `@id` annotated field of type `long`

##Restrictions.

Orm is still a beta version, this include some limitations.
* Only simple long primary key is supported.
* Version field only support basic auto incremented field. 
* Inner class are not supported (yet).
* Foreign keys are not supported (yet).
* Foreign object loading is not supported (and probably never will).
* Database visioning is not supported (?).