package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import javax.sound.sampled.BooleanControl;
import java.util.Set;

public class CorrelationMechanism {

    private Set<BoolCondition> is_send, is_receive;
    private String[] msg_inst_id, msg_name;


    public CorrelationMechanism(Set<BoolCondition> is_send, Set<BoolCondition> is_receive, String[] msg_inst_id, String[]  msg_name){
        setIs_send(is_send);
        setIs_receive(is_receive);
        setMsg_name(msg_name);
        setMsg_inst_id(msg_inst_id);
    }

    public Set<BoolCondition> getIs_send() {
        return is_send;
    }

    public void setIs_send(Set<BoolCondition> is_send) {
        this.is_send = is_send;
    }

    public Set<BoolCondition> getIs_receive() {
        return is_receive;
    }

    public void setIs_receive(Set<BoolCondition> is_receive) {
        this.is_receive = is_receive;
    }

    public String[] getMsg_inst_id() {
        return msg_inst_id;
    }

    public void setMsg_inst_id(String[] msg_inst_id) {
        this.msg_inst_id = msg_inst_id;
    }

    public String[] getMsg_name() {
        return msg_name;
    }

    public void setMsg_name(String[] msg_name) {
        this.msg_name = msg_name;
    }

    public class BoolCondition {

        private String var, op, con;

        public boolean evaluate(String var_value){
            int res = var_value.compareTo(con);
            switch (op){
                case "=":
                    return res == 0;
                case ">":
                    return res > 0;
                case "<":
                    return res < 0;
            }
            throw new IllegalArgumentException("invalid operator");
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public String getCon() {
            return con;
        }

        public void setCon(String con) {
            this.con = con;
        }

        public BoolCondition(String var, String op, String con){
            setCon(con);
            setVar(var);
            setOp(op);
        }
    }
}
