# Maktab Davomat Nazorati

Kechikkan o‘quvchilarni sinf, sana, vaqt va sabab bo‘yicha qayd etuvchi mobilga mos web-ilova.

## Fayl tuzilmasi

- `index.html` - sahifa HTML tuzilmasi.
- `style.css` - barcha dizayn va mobil moslashuv qoidalari.
- `script.js` - ma’lumotlar, ro‘yxat, statistika, Excel eksporti va saqlash logikasi.
- `exceljs.min.js` - ruxsatnoma suratlari qo‘shilgan Excel fayllarini yaratish uchun ExcelJS kutubxonasi.
- `EXCELJS-LICENSE.txt` - ExcelJS litsenziya matni.
- `README.md` - loyiha yo‘riqnoma.

## Ishga tushirish

1. `index.html` faylini brauzerda oching.
2. Internet orqali ochilsa, SheetJS kutubxonasi CDN’dan yuklanadi.
3. Ma’lumotlar brauzerda saqlanadi; mavjud Claude muhiti database API bilan ishlasa, baza avtomatik sinxronlanadi.

## Android APK

- Tayyor APK: [`Maktab-Davomat.apk`](Maktab-Davomat.apk).
- Android loyihasining manba kodi [`android-source.zip`](android-source.zip) arxivida.
- Ilovani qayta yig‘ish uchun arxivni loyiha ildiziga oching, so‘ng Node.js, JDK 21 va Android SDK bilan `npm install` va `npx cap sync android` buyrug‘ini bajaring. Keyin `android/` papkasida `gradlew assembleDebug` buyrug‘ini ishga tushiring.

## Windows va PWA

- Windows o‘rnatish fayli: [`Maktab-Davomat-Nazorati-Setup.exe`](Maktab-Davomat-Nazorati-Setup.exe).
- Setup hozirda tijorat kod-signing sertifikatisiz qurilgan. Windows SmartScreen `noma'lum nashriyot` haqida ogohlantirsa, `Подробнее` -> `Выполнить в любом случае` ni tanlang.
- Brauzerda saytni oching, keyin brauzer menyusidan **Ilovani o‘rnatish** yoki **Install app** ni tanlang.
- Ilova ikonasi uchun `icons/icon-512.png` va PWA fayllari `manifest.webmanifest`, `sw.js` ishlatiladi.

## Asosiy imkoniyatlar

- “Kech qolish”, “Erta ketish” va “Statistika / jarima” bo‘limlari orasida tab orqali almashish.
- O‘quvchini sinf, sana, vaqt va sabab holati bilan qo‘shish.
- Bugungi, haftalik va barcha vaqt statistikasi.
- Haftada 4 marta va undan ko‘p sababsiz kech qolganlarni aniqlash.
- Dars tugashidan oldin ketgan o‘quvchilarni alohida qayd etish va ixtiyoriy ruxsatnoma suratini Excel fayliga joylash.
- Android telefonlarda iPhone uslubidagi aylantirib sana va vaqt tanlash oynasi; “Bugun” va “Hozir” tezkor tugmalari.
- Android telefonlarda sinf va o‘quvchini iPhone uslubidagi aylantiriladigan ro‘yxatdan tanlash.
- Mahalliy saqlashda erta ketganlar ro‘yxati localStorage’da, suratlar IndexedDB’da saqlanadi; umumiy baza ishlatilganda ikkalasi ham bazada saqlanadi.
- Excel (`.xlsx`) faylga ro‘yxat eksporti.
- Mobil va desktop ekranlar uchun mos dizayn.
