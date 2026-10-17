# ABER - Lost & Found app (Lab Activity 4: Application Fundamentals)

Kotlin + XML layouts, Android Studio. Everything is local (no server, no internet needed).
Built from the `ABER_HighFid.fig` design: login / sign-up, Lost and Found feeds, image search.

## Open and run
1. Android Studio > **Open** > choose this `ABER` folder (the one containing `settings.gradle.kts`).
2. Wait for Gradle sync. If Android Studio offers to install SDK Platform 34 or update the Android Gradle Plugin, accept.
3. Create/choose an emulator (any phone, API 30+; a "Google APIs" image is best for the photo picker) and press **Run**.
4. For the image-search demo, put a few photos on the emulator: drag-and-drop image files onto the emulator window
   (e.g. a wallet / tumbler / phone / water-bottle photo), then they appear in the Photo Picker.

## The two features (minimum is 2)

**1. Login & Sign-up** Mobile or email accounts, show/hide password, validation (PH mobile format, email format, password rule, confirm match, terms checkbox, duplicate account), login errors (no account / wrong password), "Forgot Password?" reset (demo), stays logged in, log out. Passwords are stored as salted SHA-256 hashes. 
**2. Image Search** Pick a photo; the app ranks the Lost/Found posts by similarity using (a) colour-histogram comparison and (b) Google ML Kit on-device image labels matched against each post's keywords. Shows detected labels and a % match bar. 


### SDG alignment
- **SDG 11 - Sustainable Cities and Communities:** a community lost-and-found tool for Baguio City makes public places such as Burnham Park and Session Road easier and safer to live in.
- **SDG 12 - Responsible Consumption and Production:** getting lost phones, wallets and tumblers back to their owners keeps useful items in use instead of being replaced.
- **SDG 16 - Peace, Justice and Strong Institutions:** accounts with validated sign-up make claims traceable, and found items point to the police station where they can be claimed.
- Image Search is the innovative part: owners often cannot describe their item in words, so they can find it by photo.

## Video checklist
Record the emulator screen (Android Studio's Running Devices > record icon, or any screen recorder), with audio narration of each feature.
1. **Sign up:** show the errors first (empty fields, bad number `0123`, weak password, mismatched confirm, unchecked terms), toggle the eye icon, switch to "Sign up with email", then create the account.
2. **Log in / log out:** log out from the top-right menu, try a wrong password, a number with no account, then a correct login. Show "Login with email" and "Forgot Password?".
3. **Image search:** open the Lost / Found tabs briefly, tap **Image search**, choose a wallet photo -> the wallet post is "Best match"; choose a phone photo -> the phone post wins. Point at the "Detected:" line.
4. Say your team's names and the SDGs in the video, and upload with the link set to public.

## Semantic issues to point out
- **Photos of items not in the sample posts** give low percentages: the matcher only compares against the 4 sample posts, and colour similarity is a rough signal (a brown wallet and a brown table look alike).
- **Reset password** does not verify ownership (no SMS/email server), so anyone with the phone could reset it. A real app would send a one-time code.
- **Accounts live on one phone only** (SharedPreferences). Another device cannot log in with them.
- The "Date found" for the phone post is *October 10, 2026* here; the Figma says Oct 22, 2026, which is after the app's "today".
