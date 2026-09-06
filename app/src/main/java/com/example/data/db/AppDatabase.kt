package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.QueryAuditLog
import com.example.data.model.ServiceCategory
import com.example.data.model.SupportResource
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SupportResource::class, QueryAuditLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindmatrix_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database in coroutine on initial database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).resourceDao().insertAll(getInitialVerifiedResources())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialVerifiedResources(): List<SupportResource> {
            return listOf(
                SupportResource(
                    id = "res-telemanas-14416",
                    organizationName = "Tele-MANAS (National Tele Mental Health Programme)",
                    serviceCategory = ServiceCategory.EMOTIONAL_COUNSELLING,
                    phone = "14416",
                    alternatePhone = "1800-891-4416",
                    website = "https://telemanas.mohfw.gov.in",
                    address = "NIMHANS, Hosur Road",
                    city = "Bengaluru",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Odia, Assamese",
                    availability = "24/7 Free & Confidential",
                    eligibility = "Any individual experiencing emotional distress, grief, anxiety, or trauma",
                    sourceAuthority = "Ministry of Health & Family Welfare (MoHFW), Govt of India",
                    description = "Govt of India's flagship 24/7 toll-free mental health helpline providing psychological support by certified counsellors and psychiatrists in 20+ languages.",
                    verificationDate = "2026-02-15",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Official MoHFW Gazette & Direct Call Testing",
                    reviewer = "SIH Verification Lead",
                    notes = "National network across all 36 States/UTs with NIMHANS apex coordination",
                    keywords = "mental health, counselling, depression, anxiety, talk, loneliness, emotional support, stress, trauma, grief, telemanas, pagal, pareshan, dukh, ro raha",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-kiran-18005990019",
                    organizationName = "KIRAN Helpline (Mental Health Rehabilitation)",
                    serviceCategory = ServiceCategory.EMOTIONAL_COUNSELLING,
                    phone = "1800-599-0019",
                    alternatePhone = "",
                    website = "https://disabilityaffairs.gov.in",
                    address = "Department of Empowerment of Persons with Disabilities",
                    city = "New Delhi",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, Tamil, Telugu, Malayalam, Marathi, Gujarati, Odia, Bengali",
                    availability = "24/7 Free & Confidential",
                    eligibility = "Individuals and families seeking psycho-social rehabilitation and support",
                    sourceAuthority = "Ministry of Social Justice & Empowerment, Govt of India",
                    description = "24/7 national helpline dedicated to early screening, psychological first aid, distress management, and mental wellbeing support in 13 languages.",
                    verificationDate = "2026-02-10",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Govt Directory & Live Verification",
                    reviewer = "SIH Clinical Reviewer",
                    notes = "Operated by DEPwD with 25 national rehabilitation institutions",
                    keywords = "counselling, rehab, distress, panic, psychologist, advice, someone to talk to, kiran, baat karni hai, madad chahiye",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-erss-112",
                    organizationName = "112 National Emergency Response Support System (ERSS)",
                    serviceCategory = ServiceCategory.EMERGENCY_ASSISTANCE,
                    phone = "112",
                    alternatePhone = "100",
                    website = "https://112.gov.in",
                    address = "Emergency Response Centre, State Police Headquarters",
                    city = "All Districts",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, All State Languages",
                    availability = "24/7 Immediate Emergency Dispatch",
                    eligibility = "Immediate physical danger, violence, accidents, medical crisis, or police requirement",
                    sourceAuthority = "Ministry of Home Affairs (MHA), Govt of India",
                    description = "Single unified pan-India emergency number for immediate Police, Fire, Medical, and Disaster response services with GPS-based quick dispatch.",
                    verificationDate = "2026-02-25",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "MHA ERSS National System Standard",
                    reviewer = "SIH Verification Lead",
                    notes = "Integrated across all 28 States and 8 Union Territories",
                    keywords = "emergency, police, danger, attack, violence, physical danger, accident, ambulance, khatra, bachao, turant madad",
                    isEmergencyService = true,
                    isActive = true
                ),
                SupportResource(
                    id = "res-ncw-7827170170",
                    organizationName = "National Commission for Women (NCW) 24/7 Helpline",
                    serviceCategory = ServiceCategory.LEGAL_ASSISTANCE,
                    phone = "7827170170",
                    alternatePhone = "1091",
                    website = "https://ncw.nic.in",
                    address = "Plot No. 21, Jasola Institutional Area",
                    city = "New Delhi",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, Regional",
                    availability = "24/7 Calling & WhatsApp",
                    eligibility = "Women facing violence, domestic abuse, harassment, or requiring legal counseling",
                    sourceAuthority = "National Commission for Women, Govt of India",
                    description = "Dedicated round-the-clock emergency and legal aid cell for women affected by violence, domestic disputes, cyber harassment, and legal counseling.",
                    verificationDate = "2026-02-18",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "NCW Official Portal & Direct Telephonic Check",
                    reviewer = "SIH Legal Reviewer",
                    notes = "Direct coordination with state police, one stop centres, and legal authorities",
                    keywords = "women, domestic violence, harassment, abuse, legal aid, protection, ncw, mahila, marpeet, gharelu hinsa",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-ndma-1078",
                    organizationName = "National Disaster Management Authority (NDMA) Control Room",
                    serviceCategory = ServiceCategory.EMERGENCY_ASSISTANCE,
                    phone = "1078",
                    alternatePhone = "011-26701728",
                    website = "https://ndma.gov.in",
                    address = "NDMA Bhawan, A-1, Safdarjung Enclave",
                    city = "New Delhi",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi",
                    availability = "24/7 Disaster Response & Coordination",
                    eligibility = "Communities and individuals impacted by floods, earthquakes, landslides, cyclones, or severe calamities",
                    sourceAuthority = "National Disaster Management Authority, Govt of India",
                    description = "Official national emergency helpline for natural and human-induced disaster relief, evacuation status, rescue coordination, and early warnings.",
                    verificationDate = "2026-02-12",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "NDMA Official Standard & Direct Phone Check",
                    reviewer = "SIH Verification Lead",
                    notes = "Connects directly to NDRF and State Disaster Control Rooms",
                    keywords = "disaster, flood, earthquake, cyclone, trapped, evacuation, rescue, relief, baadh, bhukamp, aafat",
                    isEmergencyService = true,
                    isActive = true
                ),
                SupportResource(
                    id = "res-childline-1098",
                    organizationName = "Childline India / National Child Helpline (Mission Vatsalya)",
                    serviceCategory = ServiceCategory.SOCIAL_SUPPORT,
                    phone = "1098",
                    alternatePhone = "",
                    website = "https://wcd.nic.in",
                    address = "Ministry of Women & Child Development, Shastri Bhawan",
                    city = "Pan-India",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, All Indian Languages",
                    availability = "24/7 Free & Confidential",
                    eligibility = "Children in need of care, protection, shelter, or intervention, and concerned citizens",
                    sourceAuthority = "Ministry of Women and Child Development (MWCD), Govt of India",
                    description = "24-hour nationwide emergency phone outreach service for children in distress, providing shelter, rescue, medical aid, and rehabilitation.",
                    verificationDate = "2026-02-14",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "MWCD Notification & National Directory Verification",
                    reviewer = "SIH Child Safety Lead",
                    notes = "Integrated with 112 emergency response network",
                    keywords = "child, minor, child abuse, runaway, lost child, shelter, orphan, bachha, bache ki madad, balak",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-nalsa-15100",
                    organizationName = "NALSA Free Legal Aid & Services Helpline",
                    serviceCategory = ServiceCategory.LEGAL_ASSISTANCE,
                    phone = "15100",
                    alternatePhone = "011-23382778",
                    website = "https://nalsa.gov.in",
                    address = "Supreme Court of India Complex",
                    city = "New Delhi",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, All State Languages",
                    availability = "Mon-Sat, 9:30 AM - 5:30 PM",
                    eligibility = "Women, children, SC/ST, victims of trafficking/disasters, disabled, and low-income citizens",
                    sourceAuthority = "National Legal Services Authority (NALSA)",
                    description = "Free statutory legal services authority providing free advocate representation, legal advice, FIR assistance, and court dispute resolution.",
                    verificationDate = "2026-02-16",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "NALSA Portal Verification & Supreme Court Registry Record",
                    reviewer = "SIH Legal Reviewer",
                    notes = "Coordinates with 36 State Legal Services Authorities (SLSA) and District Courts",
                    keywords = "lawyer, legal aid, court, fir, free lawyer, justice, rights, kanuni sahayata, kanoon, vakil",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-vandrevala-9999666555",
                    organizationName = "Vandrevala Foundation Mental Health Helpline",
                    serviceCategory = ServiceCategory.EMOTIONAL_COUNSELLING,
                    phone = "9999666555",
                    alternatePhone = "",
                    website = "https://vandrevalafoundation.com",
                    address = "Mumbai / New Delhi Center",
                    city = "Mumbai",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, Gujarati, Marathi, Tamil, Telugu, Malayalam",
                    availability = "24/7 Free & Confidential",
                    eligibility = "Anyone in psychological distress, depression, relationship crisis, or acute anxiety",
                    sourceAuthority = "Cyrus & Priya Vandrevala Foundation (Recognized NGO Partner)",
                    description = "Round-the-clock telephone and WhatsApp counselling service staffed by experienced clinical psychologists and trained mental health counselors.",
                    verificationDate = "2026-02-21",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Helpline Live Testing & Clinical Certification Review",
                    reviewer = "SIH Verification Lead",
                    notes = "Free tele-counselling with high user satisfaction ratings",
                    keywords = "depression, sadness, suicide prevention, anxiety, psychologist, talk, help, hopeless, dil ghabra raha",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-sakhi-181",
                    organizationName = "Sakhi One Stop Centre (OSC) Helpline",
                    serviceCategory = ServiceCategory.SOCIAL_SUPPORT,
                    phone = "181",
                    alternatePhone = "",
                    website = "https://wcd.nic.in/schemes/one-stop-centre-scheme-1",
                    address = "District Hospital Complexes across India",
                    city = "Over 700 Districts",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, All Local Dialects",
                    availability = "24/7 Integrated Emergency Care",
                    eligibility = "Women affected by violence in private and public spaces",
                    sourceAuthority = "Ministry of Women & Child Development, Govt of India",
                    description = "Integrated support center providing under-one-roof medical assistance, temporary safe shelter, police facilitation, legal counseling, and psychological support.",
                    verificationDate = "2026-02-22",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Govt District Resource Verification",
                    reviewer = "SIH Social Worker Reviewer",
                    notes = "Operational in over 700 districts across all states in India",
                    keywords = "shelter, safe house, domestic violence, women emergency, medical aid, stay, ashirwad, aashray, rahne ki jagah",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-elderline-14567",
                    organizationName = "Elder Line (National Helpline for Senior Citizens)",
                    serviceCategory = ServiceCategory.SOCIAL_SUPPORT,
                    phone = "14567",
                    alternatePhone = "",
                    website = "https://elderline.dosje.gov.in",
                    address = "National Institute of Social Defence, MoSJE",
                    city = "Pan-India",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Punjabi, Regional Languages",
                    availability = "8:00 AM - 8:00 PM, 7 Days a Week",
                    eligibility = "Senior citizens (aged 60+) facing abandonment, harassment, abuse, or isolation",
                    sourceAuthority = "Ministry of Social Justice & Empowerment, Govt of India",
                    description = "Free toll-free helpline providing information, guidance, emotional support, rescue of abandoned elderly persons, and legal support.",
                    verificationDate = "2026-02-19",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Official Portal & Call Audit",
                    reviewer = "SIH Senior Support Lead",
                    notes = "Field intervention officers available in key metropolitan zones",
                    keywords = "elderly, senior citizen, old age, abandonment, old age home, bujurg, mata pita ki madad",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-icall-9152987821",
                    organizationName = "iCALL Psychosocial Helpline (TISS)",
                    serviceCategory = ServiceCategory.EMOTIONAL_COUNSELLING,
                    phone = "9152987821",
                    alternatePhone = "022-25521111",
                    website = "https://icallhelpline.org",
                    address = "Tata Institute of Social Sciences, V.N. Purav Marg, Deonar",
                    city = "Mumbai",
                    state = "National",
                    locationScope = "National",
                    supportedLanguages = "English, Hindi, Marathi, Gujarati, Malayalam, Bengali",
                    availability = "Mon-Sat, 8:00 AM - 10:00 PM",
                    eligibility = "Individuals experiencing emotional, relationship, identity, or psychosocial concerns",
                    sourceAuthority = "Tata Institute of Social Sciences (TISS Mumbai)",
                    description = "Professional, free, confidential psychosocial tele-counselling led by qualified psychologists adhering to trauma-informed and survivor-centered practices.",
                    verificationDate = "2026-02-05",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "Academic Institution Audit & Telephonic Test",
                    reviewer = "SIH Verification Lead",
                    notes = "Established pioneer in non-judgmental rights-based counseling",
                    keywords = "counselling, trauma, identity, depression, relationship, grief, tiss, icall, mental health",
                    isEmergencyService = false,
                    isActive = true
                ),
                SupportResource(
                    id = "res-seoc-1070",
                    organizationName = "State Emergency Operation Centre (Disaster Management)",
                    serviceCategory = ServiceCategory.EMERGENCY_ASSISTANCE,
                    phone = "1070",
                    alternatePhone = "",
                    website = "https://ndmindia.mha.gov.in",
                    address = "State Relief Commissioner Office, Capital Complex",
                    city = "State Capitals",
                    state = "National",
                    locationScope = "State-Level",
                    supportedLanguages = "English, Hindi, State Official Language",
                    availability = "24/7 State Emergency Desk",
                    eligibility = "Individuals affected by localized emergency situations, heavy rain, landslides, fire, or collapse",
                    sourceAuthority = "State Disaster Management Authorities (SDMA)",
                    description = "Direct emergency link to State Relief Commissioners and local administrative disaster machinery during acute civil and meteorological crises.",
                    verificationDate = "2026-01-28",
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationMethod = "SDMA Official Portal Listing",
                    reviewer = "SIH Verification Lead",
                    notes = "Reroutes to designated district emergency response cells",
                    keywords = "disaster, state emergency, flood, collapse, landslide, relief, aapatkaal",
                    isEmergencyService = true,
                    isActive = true
                )
            )
        }
    }
}
