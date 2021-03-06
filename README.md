# BASIC-ORM
**Beta** Android database helper using Annotation processor and Sqlite database.

Orm does not behave like 'traditional' orm libraries. Entities are not attached to any kind of 'state' or 'session'.
 
Perform create/read/update/delete operation as simple as you can and leave Basic-orm generate all the boring and error prone code.

No invasive code or reflection, just compile time generated code and sql.
It comes with some db optimisations like prepared statements, caching, Hibernate Criteria like querying (todo), and version control.

## Usage

Begin by Annotate your class with `@Model` than define properties with `@Property`. 

Example :
```java
@Model
public class Castle {
    
    @Id
    long id;
    
    @Property
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
```xml
...
   <application>
        ...
        <meta-data android:name="orm.dbName" android:value="{name}"/>
        ....
    </application>
...
```

Usage :

```java
// Get orm instance.
Orm orm = Orm.getInstance(context);

// Create a new castle.
Castle castle = new Castle("Camelot");
long id = orm.insertInTx(Castle.class, foo);

// Load from db.
castle = orm.loadById(Castle.class, id)

// Update the castle's name.
castle.setName("Of AAAARRrrrrghhhhhh");
orm.updateInTx(Castle.class, foo);

// Delete.
orm.deleteInTx(Castle.class, castle);
```

and **That's it.**

## Basic Rules

Field type must be one of : 
* Primitives  (`boolean`, `long`, `double`, `int`, `float`, `short`, `byte`, `char`, `short`)
* Primitives wrapper (`Integer`, `Boolean`...)
* `String` or `java.util.Date`
* `byte[]`

Field visibility must be `public` or `default` (package protected).
Class must provide a `public` or `default` (package protected) no-arguments constructor.
Entity must have a unique `@id` annotated field of type `long`

## Restrictions

Orm is still a beta version, this include some limitations.
* Composite primary key aren't supported.
* Primary key can only be of type long.
* Version only supported for incremented field. 
* Inner class aren't supported (yet).
* Foreign object loading is not supported (and probably never will).
* Database visioning is not supported (?).

## License

```
Copyright 2015 Cyrille Sondag

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```