package net.apnic.rdap.conformance.test.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpStatus;
import org.apache.http.Header;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.common.collect.Sets;

import net.apnic.rdap.conformance.Specification;
import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;

import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.ObjectTest;
import net.apnic.rdap.conformance.ResponseTest;
import net.apnic.rdap.conformance.responsetest.StatusCode;
import net.apnic.rdap.conformance.responsetest.NotStatusCode;
import net.apnic.rdap.conformance.responsetest.ContentType;
import net.apnic.rdap.conformance.ContentTest;
import net.apnic.rdap.conformance.contenttest.Array;
import net.apnic.rdap.conformance.contenttest.RdapConformance;
import net.apnic.rdap.conformance.contenttest.ScalarAttribute;
import net.apnic.rdap.conformance.contenttest.Notices;
import net.apnic.rdap.conformance.contenttest.StandardResponse;
import net.apnic.rdap.conformance.contenttest.UnknownAttributes;
import net.apnic.rdap.conformance.contenttest.BooleanValue;
import net.apnic.rdap.conformance.Utils;

public class Search implements net.apnic.rdap.conformance.Test
{
    private String url_path;
    private String test_name;
    private String search_results_key;
    private ContentTest content_test;

    public Search(ContentTest arg_content_test,
                  String arg_url_path,
                  String arg_test_name,
                  String arg_search_results_key)
    {
        url_path  = arg_url_path;
        test_name = arg_test_name;
        search_results_key = arg_search_results_key;
        content_test = arg_content_test;

        if (test_name == null) {
            test_name = "common.search";
        }
    }

    public boolean run(Context context)
    {
        List<Result> results = context.getResults();

        String bu = context.getSpecification().getBaseUrl();
        String path = bu + url_path;

        Result proto = new Result(Status.Notification, path,
                                  test_name,
                                  "", "", "", "");
        Result r = new Result(proto);
        r.setCode("response");

        Map root = Utils.standardRequest(context, path, r);
        if (root == null) {
            return false;
        }

        List<ContentTest> tests =
            new ArrayList<ContentTest>(Arrays.asList(
                new Array(content_test, search_results_key),
                new ScalarAttribute("resultsTruncated",
                                    new BooleanValue()),
                new StandardResponse()
            ));

        boolean res = true;
        HashSet<String> known_attributes = new HashSet<String>();
        for (ContentTest test : tests) {
            boolean res_inner = test.run(context, proto, root);
            if (!res_inner) {
                res = false;
            }
            known_attributes.addAll(test.getKnownAttributes());
        }

        ContentTest ua = new UnknownAttributes(known_attributes);
        boolean ret2 = ua.run(context, proto, root);
        return (res && ret2);
    }
}