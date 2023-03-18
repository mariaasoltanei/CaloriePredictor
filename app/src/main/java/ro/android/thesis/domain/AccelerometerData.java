package ro.android.thesis.domain;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import java.util.Date;

public class AccelerometerData extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;
    private String userId;
    private float x;
    private float y;
    private float z;
    private Date timestamp;

    public AccelerometerData() {
    }

    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
