package ro.android.thesis;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;
public class Car extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;
    private String age;
    @Required
    private String name;
    // Standard getters & setters
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}