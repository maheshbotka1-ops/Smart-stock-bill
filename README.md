# SmartStock GST

Starter Android project for BHAVANISRI FOODS AND BEVERAGES PRIVATE LIMITED.

## Current starter features
- Add products with HSN, opening stock, selling price and GST rate
- View product stock
- Create a basic single-item sale preview
- Automatically reduce stock after a sale
- Save invoices locally on the Android device
- GitHub Actions workflow to build a debug APK

## Company details configured
- Seller: BHAVANISRI FOODS AND BEVERAGES PRIVATE LIMITED
- GSTIN: 36AAMCB3117E2ZX
- Address: Block No. 405, Ameerpet, Hyderabad, Telangana - 500038

## Build the APK
The GitHub Actions workflow runs on a push to `main` or can be started manually from the repository's Actions tab. When the run succeeds, download the `SmartStock-GST-debug-apk` artifact from that workflow run.

## Important limitations
This is an early starter/MVP, not a finished or GST-compliance-verified billing system. It currently supports one product per invoice and a combined GST amount; it does not yet implement CGST/SGST vs IGST selection, PDF/print, amount-in-words, purchase entries, customer/product editing, staff roles, data export/backup, or shared Windows/Android database. Test carefully and have a tax professional verify invoice calculations and required fields before issuing real tax invoices.
