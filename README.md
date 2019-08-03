# api-config-chipsalliance
A Scala library for Context-Dependent Evironments, where a key-value environment is passed down a module hierarchy and each returned value depends on the query’s origin as well as the key. CDE is provably superior to existing parameterization schemes because it avoids introducing non-local source code changes when a design is modified, while also enabling features for large-scale design space exploration of compositions of generators.

## Parameterization

Parameters objects are the core abstraction of the CDE library.
Fundementally they are wrappers around Scala's partial functions.
You can look up a specific parameter by querying a Parameters object with a key, and if any case in the partial function's cases match the key you provide, the parameters object will return the supplied value. Critically, this supplied value can be dependent on any other parameters defined where the lookup is performed.

### Looking up a key's value

Assuming some:

     abstract val p: Parameters

We can ask it to supply an Int value for the string key "MY_KEY":

     val myInt = p[Int]("MY_KEY")

Note that keys and values can both be of *any* type. If the parameters object does not contain key MY_KEY, or if the object returned cannot be dynamically cast to an instance of the specified type, an exception will be thrown.

We provide a shorthand of the above for more concise access and to enable compile time checking for key collisions amongst libraries:

     case object MyKey extends Field[Int]
     val myInt = p(MyKey)

### Modifying a key's value

As the Parameters environment is passed around a Module hierarchy, it can be modified to override a key's value.
The simplest syntax is to use a new partial function to override specific values:

     val child = p.alterPartial({case MyKey => 2})

The altered environment can then be passed to child Modules.

### site

The sole additional feature of a CDE over a regular environment is a special object, called site, that dynamically points to the originating CDE of the parameter query.
This site functionality is extremely useful in the context of hardware generation because it allows for specialization based on contextual or geographic information that is injected into the generator by any intermediate node in the module hierarchy. This ability makes it possible to compose designs by defining parameters that depend on other parameters.

     case object MyLocation extends Field[String]
     val p = Parameters.empty.alter((pname, site, here) => pname match {
       case MyInt => site(MyLocation) match {
         case "icache" => 128
         case "dcache" => 256
       }
     })

     val ic = Module(new Icache(p.alterPartial({case MyLocation => "icache"})))
     val dc = Module(new Dcache(p.alterPartial({case MyLocation => "dcache"})))

By specializing the value of MyInt based on where it is called from, we can minimize the amount of code that has to be changed as we add additional locations that use MyInt, or interpose additional modules into the hierarchy between the top-level Module and the instantiation of ic and dc.

## Design Space Exploration with Knobs and Constraints

Knobs are abstractions of unbound parameters (independent variables) in the design and are used to interface with design space exploration tools.

Constraints are expressions that set limits on Parameters and Knobs and can be used to specify design requirements and bound design-space exploration.

## Configs

Config objects provide a way to represent particular configurations of a design as Scala source code. They contain the top-level Parameters environment that contains any values not bound within the design, bindings for Knob values left free by the Parameters, and any top-level constraints that are to be applied to design space exploration.
