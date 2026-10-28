package com.bhavanisri.smartstockgst;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** SmartStock GST starter MVP. Stores data locally on this Android device. */
public class MainActivity extends Activity {
    private static final String PREFS = "smartstock_gst";
    private JSONArray products = new JSONArray(), invoices = new JSONArray();
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        loadData();
        showHome();
    }

    private void loadData() {
        try {
            products = new JSONArray(getSharedPreferences(PREFS, MODE_PRIVATE).getString("products", "[]"));
            invoices = new JSONArray(getSharedPreferences(PREFS, MODE_PRIVATE).getString("invoices", "[]"));
        } catch (Exception ignored) { products = new JSONArray(); invoices = new JSONArray(); }
    }

    private void saveData() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putString("products", products.toString())
            .putString("invoices", invoices.toString()).apply();
    }

    private void base(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        scroll.addView(root);
        setContentView(scroll);
        TextView heading = new TextView(this);
        heading.setText(title); heading.setTextSize(24); heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading);
        TextView company = new TextView(this);
        company.setText("BHAVANISRI FOODS AND BEVERAGES PRIVATE LIMITED\nGSTIN: 36AAMCB3117E2ZX\nBlock No. 405, Ameerpet, Hyderabad, Telangana - 500038");
        company.setPadding(0, 12, 0, 16); root.addView(company);
    }

    private void button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(v -> action.run()); root.addView(b);
    }
    private EditText field(String hint) {
        EditText e = new EditText(this); e.setHint(hint); root.addView(e); return e;
    }
    private void addText(String value) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(16); t.setPadding(0, 12, 0, 12); root.addView(t);
    }
    private String money(double v) { return String.format(Locale.US, "%.2f", v); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private void showHome() {
        base("SmartStock GST");
        addText("Products: " + products.length() + "     Invoices: " + invoices.length());
        button("Add Product / Opening Stock", this::showAddProduct);
        button("Products & Current Stock", this::showProducts);
        button("Create Sale Invoice", this::showSale);
        button("Saved Invoices", this::showInvoices);
        addText("Starter MVP: data is stored on this phone. PDF printing, backups, staff logins, Windows sync, purchase records and full GST invoice rules are not included yet.");
    }

    private void showAddProduct() {
        base("Add Product");
        EditText name = field("Product name");
        EditText hsn = field("HSN code");
        EditText qty = field("Opening quantity");
        EditText rate = field("Selling rate (₹)");
        EditText gst = field("GST rate % (e.g. 5, 12, 18)");
        button("Save Product", () -> {
            try {
                if (name.getText().toString().trim().isEmpty()) { toast("Enter product name"); return; }
                double q = Double.parseDouble(qty.getText().toString());
                double r = Double.parseDouble(rate.getText().toString());
                double g = Double.parseDouble(gst.getText().toString());
                if (q < 0 || r < 0 || g < 0 || g > 100) { toast("Check quantity, rate and GST"); return; }
                JSONObject p = new JSONObject();
                p.put("name", name.getText().toString().trim());
                p.put("hsn", hsn.getText().toString().trim());
                p.put("qty", q); p.put("rate", r); p.put("gst", g);
                products.put(p); saveData(); toast("Product saved"); showHome();
            } catch (Exception e) { toast("Enter valid quantity, rate and GST values"); }
        });
        button("Back", this::showHome);
    }

    private void showProducts() {
        base("Products & Stock");
        if (products.length() == 0) addText("No products added yet.");
        for (int n = 0; n < products.length(); n++) {
            JSONObject p = products.optJSONObject(n);
            if (p != null) addText(p.optString("name") + " | HSN " + p.optString("hsn") +
                " | Qty: " + p.optDouble("qty") + " | Rate ₹" + money(p.optDouble("rate")) +
                " | GST " + p.optDouble("gst") + "%");
        }
        button("Back", this::showHome);
    }

    private void showSale() {
        base("Create Sale Invoice");
        if (products.length() == 0) { addText("Please add a product first."); button("Back", this::showHome); return; }
        EditText customer = field("Customer name");
        EditText customerGstin = field("Customer GSTIN (optional)");
        Spinner picker = new Spinner(this);
        String[] names = new String[products.length()];
        for (int n = 0; n < products.length(); n++) names[n] = products.optJSONObject(n).optString("name");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter); root.addView(picker);
        EditText qty = field("Quantity to sell");
        button("Save Invoice & Deduct Stock", () -> {
            try {
                JSONObject p = products.getJSONObject(picker.getSelectedItemPosition());
                double q = Double.parseDouble(qty.getText().toString());
                double available = p.optDouble("qty");
                if (q <= 0 || q > available) { toast("Quantity must be positive and no more than available stock (" + available + ")"); return; }
                double rate = p.optDouble("rate"), gst = p.optDouble("gst");
                double taxable = q * rate, tax = taxable * gst / 100.0, total = taxable + tax;
                p.put("qty", available - q);
                JSONObject inv = new JSONObject();
                inv.put("number", "SS-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()));
                inv.put("date", new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date()));
                inv.put("customer", customer.getText().toString().trim());
                inv.put("customerGstin", customerGstin.getText().toString().trim());
                inv.put("product", p.optString("name")); inv.put("hsn", p.optString("hsn"));
                inv.put("qty", q); inv.put("rate", rate); inv.put("taxable", taxable);
                inv.put("gstRate", gst); inv.put("tax", tax); inv.put("total", total);
                invoices.put(inv); saveData(); showInvoice(inv);
            } catch (Exception e) { toast("Enter a valid quantity"); }
        });
        button("Back", this::showHome);
    }

    private void showInvoices() {
        base("Saved Invoices");
        if (invoices.length() == 0) addText("No invoices saved.");
        for (int n = invoices.length() - 1; n >= 0; n--) {
            JSONObject i = invoices.optJSONObject(n);
            if (i != null) addText(i.optString("number") + " | " + i.optString("customer") + " | ₹" + money(i.optDouble("total")));
        }
        button("Back", this::showHome);
    }

    private void showInvoice(JSONObject i) {
        base("Invoice Preview");
        addText("Invoice No: " + i.optString("number") + "\nDate: " + i.optString("date") +
            "\nSeller: BHAVANISRI FOODS AND BEVERAGES PRIVATE LIMITED" +
            "\nGSTIN: 36AAMCB3117E2ZX" +
            "\nBuyer: " + i.optString("customer") + "\nBuyer GSTIN: " + i.optString("customerGstin") +
            "\n\nItem: " + i.optString("product") + "\nHSN: " + i.optString("hsn") +
            "\nQty: " + i.optDouble("qty") + "\nRate: ₹" + money(i.optDouble("rate")) +
            "\nTaxable value: ₹" + money(i.optDouble("taxable")) +
            "\nGST " + i.optDouble("gstRate") + "%: ₹" + money(i.optDouble("tax")) +
            "\n\nTOTAL: ₹" + money(i.optDouble("total")) +
            "\n\nPreview only. Confirm intra/inter-state tax split, invoice particulars and tax treatment with your accountant before issuing tax invoices.");
        button("Back to Home", this::showHome);
    }
}
