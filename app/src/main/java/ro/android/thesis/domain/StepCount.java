package ro.android.thesis.domain;

import org.bson.types.ObjectId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class StepCount extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;
    private String userId;

    private Date timestamp;
    private long noSteps;

    public StepCount() {
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setNoSteps(long noSteps) {
        this.noSteps = noSteps;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getNoSteps() {
        return noSteps;
    }
}
