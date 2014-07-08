package net.apnic.rdap.conformance.valuetest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.ValueTest;
import net.apnic.rdap.conformance.Utils;

public class MaxSigLife implements ValueTest
{
    public MaxSigLife() { }

    public boolean run(Context context, Result proto,
                       Object arg_data)
    {
        Result nr = new Result(proto);

        boolean res = true;
        Integer value = null;
        try {
            Double dvalue = (Double) arg_data;
            if ((dvalue != null) && (dvalue == Math.rint(dvalue))) {
                value = new Integer((int) Math.round(dvalue));
            }
        } catch (ClassCastException ce) {
        }

        if (value == null) {
            nr.setStatus(Status.Failure);
            nr.setInfo("not integer");
            res = false;
        } else {
            nr.setStatus(Status.Success);
            nr.setInfo("is integer");
        }
        context.addResult(nr);

        Result cvr = new Result(proto);
        if (value != null) {
            if (value < 1) {
                cvr.setStatus(Status.Failure);
                cvr.setInfo("not positive");
                res = false;
            } else {
                cvr.setStatus(Status.Success);
                cvr.setInfo("positive");
            }
            context.addResult(cvr);
        }

        return res;
    }

    public Set<String> getKnownAttributes()
    {
        return Sets.newHashSet();
    }
}