package chase.minecraft.architectury.warpmod.data;

import net.minecraft.network.chat.Component;

// This is defining a Java record class named `WarpTravelParemeters` with two fields: `distance` of type `int` and `direction` of type `Component`. The `record` keyword is a new feature in Java 16 that allows for concise classes that are primarily used to store data. It automatically generates constructors, accessors, and `equals()` and `hashCode()` methods based on the fields defined in the record.
public record WarpTravelParemeters(int distance, Component direction)
{

}
