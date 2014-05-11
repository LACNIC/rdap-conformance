package net.apnic.rdap.conformance.contenttest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.ContentTest;
import net.apnic.rdap.conformance.Utils;

public class BooleanValue implements ContentTest
{
    public BooleanValue() { }

    public boolean run(Context context, Result proto,
                       Object arg_data)
    {
        Result nr = new Result(proto);

        boolean res = true;
        Boolean value = null;
        try {
            value = (Boolean) arg_data;
        } catch (ClassCastException ce) {
        }

        if (value == null) {
            nr.setStatus(Status.Failure);
            nr.setInfo("not boolean");
            res = false;
        } else {
            nr.setStatus(Status.Success);
            nr.setInfo("is boolean");
        }
        context.addResult(nr);

        return res;
    }

    public Set<String> getKnownAttributes()
    {
        return Sets.newHashSet();
    }
}