package tuanv20.mockjiraapi.Model;

public class Data {
    long time;
    float delta_az;
    float delta_el;
    int tlm_fr;
    int cmd;

    public Data(long time, float delta_az, float delta_el, int tlm_fr, int cmd){
        this.time = time;
        this.delta_az = delta_az;
        this.delta_el = delta_el;
        this.tlm_fr = tlm_fr;
        this.cmd = cmd;
    }

    public long getTime(){
        return this.time;
    }
    
     public float getDelta_az(){
        return this.delta_az;
    }

     public float getDelta_el(){
        return this.delta_el;
    }

     public int getTlm_fr(){
        return this.tlm_fr;
    }

     public int getCmd(){
        return this.cmd;
    }

    public void setTime(long time){
        this.time = time;
    }

    public void setDelta_az(float delta_az){
        this.delta_az = delta_az;
    }

    public void setDelta_el(float delta_el){
        this.delta_el = delta_el;
    }

    public void setTlm_fr(int tlm_fr){
        this.tlm_fr = tlm_fr;
    }

      public void setCmd(int cmd){
        this.cmd = cmd;
    }
}
