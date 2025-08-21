package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LearnActivity : AppCompatActivity() {
    
    private lateinit var backArrow: ImageView
    private lateinit var cardWhatIsOralCancer: CardView
    private lateinit var cardRiskFactors: CardView
    private lateinit var cardPrevention: CardView
    private lateinit var cardDiet: CardView
    private lateinit var cardExercise: CardView
    private lateinit var cardEarlyDetection: CardView
    private lateinit var cardCallToAction: CardView
    
    companion object {
        private const val TAG = "LearnActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "LearnActivity onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d(TAG, "Edge to edge enabled")
            
            setContentView(R.layout.activity_learn)
            Log.d(TAG, "Learn layout set successfully")
            
            // Note: Window insets setup removed as main ID doesn't exist in layout
            Log.d(TAG, "Window insets setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate setup: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }

        // Initialize UI elements
        initializeViews()
        
        // Setup functionality
        setupBackNavigation()
        setupCardClickListeners()
        
        Log.d(TAG, "LearnActivity onCreate completed successfully")
    }
    
    private fun initializeViews() {
        try {
            Log.d(TAG, "Initializing views...")
            
            backArrow = findViewById(R.id.backArrow)
            cardWhatIsOralCancer = findViewById(R.id.cardWhatIsOralCancer)
            cardRiskFactors = findViewById(R.id.cardRiskFactors)
            cardPrevention = findViewById(R.id.cardPrevention)
            cardDiet = findViewById(R.id.cardDiet)
            cardExercise = findViewById(R.id.cardExercise)
            cardEarlyDetection = findViewById(R.id.cardEarlyDetection)
            cardCallToAction = findViewById(R.id.cardCallToAction)
            
            Log.d(TAG, "Views initialized successfully")
            Log.d(TAG, "Back arrow: ${if (backArrow != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "What is Oral Cancer card: ${if (cardWhatIsOralCancer != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Risk Factors card: ${if (cardRiskFactors != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Prevention card: ${if (cardPrevention != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Diet card: ${if (cardDiet != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Exercise card: ${if (cardExercise != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Early Detection card: ${if (cardEarlyDetection != null) "FOUND" else "NOT FOUND"}")
            Log.d(TAG, "Call to Action card: ${if (cardCallToAction != null) "FOUND" else "NOT FOUND"}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupBackNavigation() {
        try {
            Log.d(TAG, "Setting up back navigation")
            
            if (backArrow != null) {
                backArrow.setOnClickListener {
                    Log.d(TAG, "Back arrow clicked - finishing activity")
                    finish()
                }
                
                Log.d(TAG, "Back navigation setup successful")
            } else {
                Log.e(TAG, "Back arrow not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up back navigation: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupCardClickListeners() {
        try {
            Log.d(TAG, "Setting up card click listeners")
            
            // What is Oral Cancer Card
            if (cardWhatIsOralCancer != null) {
                cardWhatIsOralCancer.setOnClickListener {
                    Log.d(TAG, "What is Oral Cancer card clicked")
                    navigateToDetail(
                        "What is Oral Cancer?",
                        getWhatIsOralCancerContent(),
                        R.drawable.logo
                    )
                }
            }
            
            // Risk Factors Card
            if (cardRiskFactors != null) {
                cardRiskFactors.setOnClickListener {
                    Log.d(TAG, "Risk Factors card clicked")
                    navigateToDetail(
                        "Risk Factors",
                        getRiskFactorsContent(),
                        R.drawable.riskfactor
                    )
                }
            }
            
            // Prevention & Precautions Card
            if (cardPrevention != null) {
                cardPrevention.setOnClickListener {
                    Log.d(TAG, "Prevention card clicked")
                    navigateToDetail(
                        "Prevention & Precautions",
                        getPreventionContent(),
                        R.drawable.sheild
                    )
                }
            }
            
            // Diet Recommendations Card
            if (cardDiet != null) {
                cardDiet.setOnClickListener {
                    Log.d(TAG, "Diet card clicked")
                    navigateToDetail(
                        "Diet Recommendations",
                        getDietContent(),
                        R.drawable.ic_medication
                    )
                }
            }
            
            // Exercise & Physical Activity Card
            if (cardExercise != null) {
                cardExercise.setOnClickListener {
                    Log.d(TAG, "Exercise card clicked")
                    navigateToDetail(
                        "Exercise & Physical Activity",
                        getExerciseContent(),
                        R.drawable.ic_trophy
                    )
                }
            }
            
            // Early Detection Signs Card
            if (cardEarlyDetection != null) {
                cardEarlyDetection.setOnClickListener {
                    Log.d(TAG, "Early Detection card clicked")
                    navigateToDetail(
                        "Early Detection Signs",
                        getEarlyDetectionContent(),
                        R.drawable.ic_result
                    )
                }
            }
            
            // Call to Action Card
            if (cardCallToAction != null) {
                cardCallToAction.setOnClickListener {
                    Log.d(TAG, "Call to Action card clicked")
                    navigateToDetail(
                        "Take Action Today",
                        getCallToActionContent(),
                        R.drawable.ic_success_check
                    )
                }
            }
            
            Log.d(TAG, "Card click listeners setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up card click listeners: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun navigateToDetail(title: String, content: String, imageResId: Int) {
        try {
            Log.d(TAG, "Navigating to DetailActivity with title: $title")
            
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_TITLE, title)
                putExtra(DetailActivity.EXTRA_CONTENT, content)
                putExtra(DetailActivity.EXTRA_IMAGE_RES_ID, imageResId)
            }
            
            startActivity(intent)
            Log.d(TAG, "Successfully started DetailActivity")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to DetailActivity: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Content methods for each section
    private fun getWhatIsOralCancerContent(): String {
        return """Oral cancer refers to cancer that develops in any part of the mouth, including the lips, tongue, gums, floor of the mouth, roof of the mouth, and the lining of the cheeks. It's a serious condition that can be life-threatening if not detected and treated early.

Early detection significantly improves treatment outcomes and survival rates. Regular self-examinations and professional check-ups are crucial for early identification.

Oral cancer can manifest in various forms, including squamous cell carcinoma, which is the most common type. The disease can affect anyone, but certain factors increase the risk significantly.

Understanding the basics of oral cancer is the first step toward prevention and early detection. Knowledge empowers individuals to make informed decisions about their health and seek timely medical attention when needed.

The mouth is a complex structure with multiple tissue types, and cancer can develop in any of these areas. Early-stage oral cancer may not cause noticeable symptoms, which is why regular screenings are essential."""
    }
    
    private fun getRiskFactorsContent(): String {
        return """Several factors can increase your risk of developing oral cancer. Understanding these risk factors can help you make informed decisions about your health and take preventive measures.

Primary Risk Factors:
• Tobacco use (smoking and chewing) - The most significant risk factor
• Excessive alcohol consumption - Especially when combined with tobacco use
• Human papillomavirus (HPV) infection - Particularly HPV-16 and HPV-18
• Poor oral hygiene - Can lead to chronic inflammation and tissue damage
• Excessive sun exposure - For lip cancer specifically
• Family history of cancer - Genetic predisposition may play a role

Additional Risk Factors:
• Age - Most cases occur in people over 40
• Gender - Men are more likely to develop oral cancer
• Diet - Low consumption of fruits and vegetables
• Weakened immune system - From conditions like HIV/AIDS
• Previous oral cancer diagnosis - Increases risk of recurrence

Understanding these risk factors is crucial for prevention and early detection. Regular check-ups are especially important if you have multiple risk factors."""
    }
    
    private fun getPreventionContent(): String {
        return """Prevention is the best approach to oral cancer. By adopting healthy habits and avoiding risk factors, you can significantly reduce your chances of developing this disease.

Key Prevention Strategies:

1. Quit Tobacco Use Completely
   - Stop smoking cigarettes, cigars, and pipes
   - Avoid chewing tobacco and snuff
   - Seek professional help if needed for cessation

2. Limit Alcohol Consumption
   - Follow recommended guidelines (1 drink/day for women, 2 for men)
   - Avoid binge drinking
   - Consider abstaining if you have other risk factors

3. Maintain Excellent Oral Hygiene
   - Brush teeth twice daily with fluoride toothpaste
   - Floss daily to remove plaque between teeth
   - Use antimicrobial mouthwash
   - Replace toothbrush every 3-4 months

4. Regular Dental Check-ups
   - Visit dentist every 6 months for professional cleaning
   - Request oral cancer screening during visits
   - Report any unusual symptoms immediately

5. Protect Against Sun Exposure
   - Use lip balm with SPF 30 or higher
   - Wear wide-brimmed hats outdoors
   - Avoid tanning beds

6. Self-Examination
   - Check mouth monthly for changes
   - Look for sores, patches, or lumps
   - Report persistent symptoms to healthcare provider

7. Healthy Lifestyle
   - Eat a balanced diet rich in fruits and vegetables
   - Exercise regularly to boost immunity
   - Get adequate sleep and manage stress

Remember: Early detection saves lives. Regular screenings and prompt attention to symptoms are crucial."""
    }
    
    private fun getDietContent(): String {
        return """A healthy diet plays a crucial role in preventing oral cancer and maintaining overall oral health. The right foods can boost your immune system and provide protective compounds.

Foods to Include:

1. Colorful Fruits and Vegetables
   - Rich in antioxidants that fight free radicals
   - Examples: berries, citrus fruits, leafy greens, carrots
   - Aim for 5-9 servings daily

2. Green Tea
   - Contains polyphenols with anti-cancer properties
   - Drink 2-3 cups daily for maximum benefit
   - Choose organic varieties when possible

3. Omega-3 Rich Foods
   - Anti-inflammatory properties
   - Sources: fatty fish (salmon, mackerel), nuts, seeds
   - Include 2-3 servings weekly

4. Whole Grains and Fiber
   - Help maintain healthy digestion
   - Examples: brown rice, quinoa, whole wheat bread
   - Choose whole grains over refined options

5. Cruciferous Vegetables
   - Contain compounds that may prevent cancer
   - Examples: broccoli, cauliflower, Brussels sprouts
   - Include in meals 3-4 times weekly

Foods to Avoid:

1. Processed and Red Meats
   - High in saturated fats and preservatives
   - Limit consumption to occasional treats
   - Choose lean protein sources instead

2. Excessively Hot or Spicy Foods
   - Can irritate oral tissues
   - May contribute to chronic inflammation
   - Allow foods to cool before eating

3. Sugary Foods and Beverages
   - Feed harmful bacteria in mouth
   - Contribute to tooth decay and gum disease
   - Choose natural sweeteners when possible

4. Alcohol (in excess)
   - Can damage oral tissues
   - Increases cancer risk when combined with tobacco
   - Limit to recommended amounts

Hydration Tips:
• Drink plenty of water throughout the day
• Avoid sugary drinks and sodas
• Green tea and herbal teas are excellent choices
• Stay hydrated to maintain healthy saliva production

Remember: A balanced diet combined with good oral hygiene provides the best protection against oral cancer."""
    }
    
    private fun getExerciseContent(): String {
        return """Regular physical activity is essential for overall health and can help reduce your risk of oral cancer by boosting immunity and reducing inflammation.

Recommended Exercise Types:

1. Cardiovascular Exercises
   - Walking: 30 minutes daily at moderate pace
   - Swimming: Low-impact, full-body workout
   - Cycling: Great for cardiovascular health
   - Aim for 150 minutes of moderate activity weekly

2. Strength Training
   - 2-3 sessions per week
   - Focus on major muscle groups
   - Use bodyweight exercises or light weights
   - Helps maintain muscle mass and bone density

3. Yoga and Meditation
   - Reduces stress and inflammation
   - Improves flexibility and balance
   - Enhances mental well-being
   - Practice 20-30 minutes daily

4. Deep Breathing Exercises
   - Improves oxygen circulation
   - Reduces stress hormones
   - Enhances immune function
   - Practice 10-15 minutes daily

5. Flexibility and Stretching
   - Improves range of motion
   - Reduces muscle tension
   - Enhances overall mobility
   - Include in daily routine

Exercise Guidelines:

Frequency: Aim for 5-6 days per week
Duration: 30-60 minutes per session
Intensity: Moderate to vigorous
Type: Mix of cardio, strength, and flexibility

Benefits for Oral Health:
• Boosts immune system function
• Reduces inflammation throughout body
• Improves circulation to oral tissues
• Helps maintain healthy weight
• Reduces stress and anxiety
• Enhances overall well-being

Safety Tips:
• Start slowly and gradually increase intensity
• Listen to your body and rest when needed
• Stay hydrated during exercise
• Consult healthcare provider before starting new program
• Stop if you experience pain or discomfort

Remember: Regular physical activity is a key component of a healthy lifestyle and can significantly reduce your risk of various health conditions, including oral cancer."""
    }
    
    private fun getEarlyDetectionContent(): String {
        return """Early detection is crucial for successful treatment of oral cancer. Knowing the warning signs and performing regular self-examinations can save your life.

Warning Signs to Watch For:

1. Persistent Mouth Sores
   - Sores that don't heal within 2 weeks
   - Painful or painless ulcers
   - Sores that bleed easily
   - White or red patches

2. White or Red Patches
   - Leukoplakia (white patches)
   - Erythroplakia (red patches)
   - Mixed white and red areas
   - Patches that feel rough or raised

3. Difficulty Swallowing or Chewing
   - Pain when swallowing
   - Feeling of food stuck in throat
   - Changes in chewing ability
   - Jaw stiffness or pain

4. Unexplained Bleeding
   - Bleeding from mouth or throat
   - Blood in saliva
   - Bleeding gums (if not due to gum disease)
   - Nosebleeds (for nasopharyngeal cancer)

5. Changes in Voice or Speech
   - Hoarseness lasting more than 2 weeks
   - Changes in voice quality
   - Difficulty speaking clearly
   - Pain when speaking

6. Lumps or Thickening
   - Lumps in mouth, neck, or throat
   - Thickening of cheek tissue
   - Swelling in jaw area
   - Changes in denture fit

7. Numbness or Pain
   - Numbness in mouth or face
   - Persistent ear pain
   - Pain in jaw or teeth
   - Tingling sensations

Self-Examination Steps:

1. Look in Mirror
   - Check lips, gums, and inside cheeks
   - Examine roof and floor of mouth
   - Look at back of throat
   - Check under tongue

2. Feel for Changes
   - Use fingers to feel for lumps
   - Check for tenderness or pain
   - Feel for rough or smooth areas
   - Note any changes in texture

3. Check Regularly
   - Perform exam monthly
   - Note any changes over time
   - Keep a record of findings
   - Report changes to healthcare provider

When to Seek Medical Attention:
• Any symptom lasting more than 2 weeks
• Unexplained bleeding or pain
• Changes in voice or speech
• Difficulty swallowing or chewing
• Lumps or thickening in mouth or neck
• Persistent sores or patches

Remember: Early detection significantly improves treatment outcomes. Don't ignore symptoms - seek professional evaluation promptly."""
    }
    
    private fun getCallToActionContent(): String {
        return """Now that you have the knowledge, it's time to take action! Your health is in your hands, and small changes today can make a big difference tomorrow.

Immediate Actions You Can Take:

1. Schedule Your Next Dental Check-up
   - Call your dentist today
   - Request an oral cancer screening
   - Set up regular 6-month appointments
   - Don't wait for symptoms to appear

2. Start Prevention Today
   - Quit tobacco use (seek help if needed)
   - Limit alcohol consumption
   - Improve oral hygiene routine
   - Begin regular exercise program

3. Make Dietary Changes
   - Add more fruits and vegetables
   - Reduce processed foods
   - Stay hydrated with water
   - Choose whole grains over refined

4. Perform Monthly Self-Exams
   - Set a reminder on your phone
   - Learn proper examination technique
   - Keep a health journal
   - Report changes immediately

5. Educate Others
   - Share information with family and friends
   - Encourage regular check-ups
   - Support tobacco cessation efforts
   - Promote healthy lifestyle choices

6. Stay Informed
   - Follow reliable health sources
   - Attend health education events
   - Join support groups if needed
   - Keep up with latest research

7. Build a Support Network
   - Connect with healthcare providers
   - Join community health programs
   - Find accountability partners
   - Seek professional help when needed

Your Health Journey Starts Now:

Remember: Prevention is always better than treatment. The choices you make today will impact your health for years to come. Take the first step now - schedule that dental appointment, make that lifestyle change, or reach out for support.

You have the power to protect your health. Start your prevention journey today!

Resources Available:
• National Cancer Institute
• American Cancer Society
• Local health departments
• Dental associations
• Support groups and hotlines

Take action today - your future self will thank you!"""
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LearnActivity onResume called")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "LearnActivity onPause called")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LearnActivity onDestroy called")
    }
}
