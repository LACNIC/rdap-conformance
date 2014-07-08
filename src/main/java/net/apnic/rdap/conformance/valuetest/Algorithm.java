package net.apnic.rdap.conformance.valuetest;

import java.util.Set;

import com.google.common.collect.Sets;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.ValueTest;

public class Algorithm implements ValueTest
{
    static Set<Integer> algorithms = Sets.newHashSet(
        3, 5, 6, 7, 8, 10, 12, 13, 14, 253, 254
    );

    public Algorithm() { }

    public boolean run(Context context, Result proto,
                       Object arg_data)
    {
        Integer value = null;
        try {
            Double dvalue = (Double) arg_data;
            if ((dvalue != null) && (dvalue == Math.rint(dvalue))) {
                value = new Integer((int) Math.round(dvalue));
            }
        } catch (ClassCastException ce) {
        }

        Result nr = new Result(proto);
        nr.setDetails((value != null), "is integer", "not integer");
        context.addResult(nr);

        if (value != null) {
            Result cvr = new Result(proto);
            boolean res = cvr.setDetails(algorithms.contains(value),
                                         "valid", "invalid");
            context.addResult(cvr);
            return res;
        } else {
            return false;
        }
    }
}
