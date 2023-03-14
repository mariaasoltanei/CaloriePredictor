//package ro.android.thesis;
//
//import androidx.annotation.NonNull;
//
//import org.bson.types.ObjectId;
//
//import io.realm.RealmObject;
//import io.realm.annotations.PrimaryKey;
//import io.realm.annotations.RealmField;
//
//public class Human extends RealmObject {
//    @PrimaryKey
//    @RealmField("_id")
//    private ObjectId _id = new ObjectId();
//    private String name = null;
//    private int age;
//
//    public Human() {
//    }
//
//    public ObjectId get_id() {
//        return _id;
//    }
//
//    public void set_id(ObjectId _id) {
//        this._id = _id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//}
