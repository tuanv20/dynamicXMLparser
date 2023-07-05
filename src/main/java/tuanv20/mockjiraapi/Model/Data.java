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

    public Data(String[] vars){
        this.time = Long.parseLong(vars[0]);
        this.delta_az = Float.parseFloat(vars[1]);
        this.delta_el = Float.parseFloat(vars[2]);
        this.tlm_fr = Integer.parseInt(vars[3]);
        this.cmd = Integer.parseInt(vars[4]);
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

    public String toString(){
        return String.format("[TIME: %d, DELTA_AZ: %.1f, DELTA_EL: %.1f, TLM_FR: %d, CMD: %d]", this.time, this.delta_az, this.delta_el, this.tlm_fr, this.cmd);
    }
}
