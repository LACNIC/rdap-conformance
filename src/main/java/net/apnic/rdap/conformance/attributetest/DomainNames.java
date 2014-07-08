package net.apnic.rdap.conformance.attributetest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.text.Normalizer;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.AttributeTest;
import net.apnic.rdap.conformance.SearchTest;
import net.apnic.rdap.conformance.Utils;

import com.google.common.collect.Sets;
import com.google.common.base.CharMatcher;
import com.vgrs.xcode.idna.Idna;
import com.vgrs.xcode.idna.Punycode;
import com.vgrs.xcode.util.XcodeException;

public class DomainNames implements SearchTest
{
    private String key = null;
    private String pattern = null;

    public DomainNames() {}

    public void setSearchDetails(String arg_key, String arg_pattern)
    {
        key = arg_key;
        pattern = arg_pattern;
    }

    private int isValidLdhName(String ldh_name)
    {
        String[] labels = ldh_name.split(".");
        Pattern ldh_pattern = Pattern.compile(
            "[\\p{Alnum}][\\p{Alnum}-]*[\\p{Alnum}]?"
        );
        boolean ldhres = true;
        boolean a_label_found = false;
        for (String label : labels) {
            boolean labelres = ldh_pattern.matcher(label).matches();
            if (!labelres) {
                ldhres = false;
            }
            if (label.length() > 63) {
                ldhres = false;
            }
            if (label.startsWith("xn--")) {
                a_label_found = true;
            }
        }

        return (ldhres ? (a_label_found ? 2 : 1) : 0);
    }

    public boolean run(Context context, Result proto,
                       Map<String, Object> data)
    {
        Result nr = new Result(proto);
        nr.setCode("content");
        nr.setDocument("draft-ietf-weirds-json-response-07");
        nr.setReference("4");

        boolean res = true;
        String ldh_name = Utils.getStringAttribute(context,
                                                   nr, "ldhName",
                                                   Status.Failure,
                                                   data);
        if (ldh_name == null) {
            return false;
        }
        int ldhres_both = isValidLdhName(ldh_name);
        boolean ldhres = (ldhres_both >= 1);
        boolean a_label_found = (ldhres_both == 2);

        Result dn = new Result(nr);
        dn.addNode("ldhName");
        if (ldhres) {
            dn.setInfo("valid");
            dn.setStatus(Status.Success);
        } else {
            dn.setInfo("invalid");
            dn.setStatus(Status.Failure);
            res = false;
        }
        context.addResult(dn);

        if (!a_label_found && (pattern != null)) {
            Result rp = new Result(nr);
            rp.addNode("ldhName");
            String ldh_pattern = pattern.replaceAll("\\*", ".*");
            ldh_pattern = ".*" + ldh_pattern + ".*";
            Pattern p = Pattern.compile(ldh_pattern,
                                        Pattern.CASE_INSENSITIVE);
            if (!p.matcher(ldh_name).matches()) {
                rp.setStatus(Status.Warning);
                rp.setInfo("response domain name does not " +
                           "match search pattern");
            } else {
                rp.setStatus(Status.Success);
                rp.setInfo("response domain name matches " +
                           "search pattern");
            }
            context.addResult(rp);
        }

        Object unicode_name_obj = data.get("unicodeName");
        if (unicode_name_obj == null) {
            if (a_label_found) {
                Result nou = new Result(nr);
                nou.addNode("unicodeName");
                nou.setStatus(Status.Warning);
                nou.setInfo("not present and ldhName contains A-label");
                context.addResult(nou);
            }
            return res;
        }

        String unicode_name = Utils.getStringAttribute(context,
                                                       nr, "unicodeName",
                                                       Status.Failure,
                                                       data);
        boolean is_ascii =
            CharMatcher.ASCII.matchesAllOf(unicode_name);
        Result hu = new Result(nr);
        hu.addNode("unicodeName");
        /* There are at least a couple of implementations that return
         * ldhName in unicodeName when ldhName contains no A-labels.
         * This is not (currently) compliant: see section 4 of the
         * draft, as well as RFC 5890 [2.3.2.1], which requires that
         * at least one U-label be present. */
        if (is_ascii) {
            hu.setInfo("no non-ascii characters present");
            hu.setStatus(Status.Failure);
            res = false;
        } else {
            hu.setInfo("non-ascii characters present");
            hu.setStatus(Status.Success);
        }
        context.addResult(hu);
        if (!res) {
            return res;
        }

        Idna idna = null;
        try {
            idna = new Idna(new Punycode(), true, true);
        } catch (XcodeException xe) {
            System.err.println("Unable to initialise/use IDNA processor " +
                               xe.toString());
            return res;
        }

        int[] unicode_nums = new int[unicode_name.length()];
        char[] unicode_chars = unicode_name.toCharArray();
        for (int i = 0; i < unicode_name.length(); i++) {
            unicode_nums[i] = unicode_chars[i];
        }
        String ldh_name_check = null;
        String error = null;
        try {
            ldh_name_check = new String(idna.domainToAscii(unicode_nums));
        } catch (XcodeException ce) {
            error = ce.toString();
        }
        Result iv = new Result(nr);
        iv.addNode("unicodeName");
        if (ldh_name_check != null) {
            iv.setInfo("valid");
            iv.setStatus(Status.Success);
        } else {
            iv.setInfo("invalid: " + error);
            iv.setStatus(Status.Failure);
            res = false;
        }
        context.addResult(iv);

        if (ldh_name_check != null) {
            Result ms = new Result(nr);
            ms.addNode("unicodeName");
            String ldh_name_canon = ldh_name.toLowerCase();
            if (ldh_name_canon.charAt(ldh_name_canon.length() - 1) != '.') {
                ldh_name_canon += ".";
            }
            if (ldh_name_check.charAt(ldh_name_check.length() - 1) != '.') {
                ldh_name_check += ".";
            }
            if (ldh_name_check.equals(ldh_name_canon)) {
                ms.setInfo("matches ldhName");
                ms.setStatus(Status.Success);
            } else {
                ms.setInfo("does not match ldhName");
                ms.setStatus(Status.Failure);
                res = false;
            }
            context.addResult(ms);
        }

        if ((unicode_name != null) && (pattern != null)) {
            Result rp = new Result(nr);
            rp.addNode("unicodeName");
            String un_pattern = pattern.replaceAll("\\*", ".*");
            un_pattern = ".*" + un_pattern + ".*";
            un_pattern =
                Normalizer.normalize(un_pattern,
                                     Normalizer.Form.NFKC);
            Pattern p = Pattern.compile(un_pattern,
                                        Pattern.CASE_INSENSITIVE |
                                        Pattern.UNICODE_CASE);
            if (!p.matcher(unicode_name).matches()) {
                rp.setStatus(Status.Warning);
                rp.setInfo("response domain name does not " +
                           "match search pattern");
            } else {
                rp.setStatus(Status.Success);
                rp.setInfo("response domain name matches " +
                           "search pattern");
            }
            context.addResult(rp);
        }

        return res;
    }

    public Set<String> getKnownAttributes()
    {
        return Sets.newHashSet("ldhName", "unicodeName");
    }
}