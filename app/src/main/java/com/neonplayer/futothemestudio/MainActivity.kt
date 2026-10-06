package com.neonplayer.futothemestudio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.io.*
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

enum class KeyGroup(val title: String) { LETTERS("أزرار الكتابة"), TOP("الشريط العلوي"), BOTTOM("الصف السفلي"), FUNCTIONAL("أزرار الوظائف"), ACTION("الإرسال والإجراء"), SPACE("المسافة"), SPECIAL("أزرار خاصة") }
enum class FillMode { SOLID, GRADIENT }

data class GroupStyle(
    val shapeId: Int = 0,
    val fillMode: FillMode = FillMode.SOLID,
    val color1: Int = 0xFFE9E0FF.toInt(),
    val color2: Int = 0xFFD2C1FF.toInt(),
    val pressed1: Int = 0xFFD2C1FF.toInt(),
    val pressed2: Int = 0xFFB39DDB.toInt(),
    val textColor: Int = 0xFF242029.toInt(),
    val imageKey: String? = null,
    val opacity: Float = 1f
)

data class ThemeState(
    val name: String = "My FUTO Theme", val author: String = "", val id: String = "custom.my.theme",
    val description: String = "Created with FUTO Theme Studio", val version: Int = 2,
    val light: Boolean = true, val primary: Int = 0xFF6750A4.toInt(), val onPrimary: Int = android.graphics.Color.WHITE,
    val background: Int = 0xFFF7F2FA.toInt(), val background2: Int = 0xFFEDE5F3.toInt(),
    val backgroundMode: FillMode = FillMode.SOLID, val backgroundImage: ByteArray? = null, val backgroundFileName: String? = null,
    val textScale: Float = 1f, val arabicTextScale: Float = 1f, val englishTextScale: Float = 1f,
    val buttonScale: Float = 1f, val spacing: Float = 5f, val radius: Float = 22f,
    val borderWidth: Float = 0f, val borderColor: Int = 0x33000000,
    val selectedKey: String = "ا", val selectedGroup: KeyGroup = KeyGroup.LETTERS,
    val groupStyles: Map<KeyGroup, GroupStyle> = defaultGroups(), val keyOverrides: Map<String, GroupStyle> = emptyMap(),
    val keyImages: Map<String, ByteArray> = emptyMap(), val keyImageNames: Map<String, String> = emptyMap(),
    val arabicFontName: String? = null, val arabicFontBytes: ByteArray? = null,
    val englishFontName: String? = null, val englishFontBytes: ByteArray? = null,
    val exportFontLanguage: String = "العربية"
)

private fun defaultGroups(): Map<KeyGroup, GroupStyle> = KeyGroup.values().associateWith { GroupStyle() }

private val arabicRows = listOf(listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"),listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك"),listOf("ئ","ء","ؤ","ر","ى","ة","و","ز","ظ","د"),listOf("123","🌐","⌫","،","مسافة","↵"))
private val englishRows = listOf(listOf("Q","W","E","R","T","Y","U","I","O","P"),listOf("A","S","D","F","G","H","J","K","L"),listOf("Z","X","C","V","B","N","M"),listOf("123","⇧","⌫",",","SPACE","↵"))
private val topBar = listOf("تراجع","إعادة","نسخ","لصق","تحديد","كل النص","GIF","😀","⚙")
private val palettes = listOf(
    "بنفسجي" to Triple(0xFF6750A4.toInt(),0xFFE9DDFF.toInt(),0xFFF7F2FA.toInt()),
    "محيطي" to Triple(0xFF006A6A.toInt(),0xFFB2EBEA.toInt(),0xFFE8F5F4.toInt()),
    "غروب" to Triple(0xFF9A3D00.toInt(),0xFFFFDCC8.toInt(),0xFFFFF3EC.toInt()),
    "غابة" to Triple(0xFF3E683B.toInt(),0xFFC7E9BF.toInt(),0xFFF1F8EE.toInt()),
    "وردي" to Triple(0xFF9B405A.toInt(),0xFFFFD9E2.toInt(),0xFFFFF0F3.toInt()),
    "جرافيت" to Triple(0xFF4B4F58.toInt(),0xFFDCE1E8.toInt(),0xFFF2F3F5.toInt()),
    "كهرماني" to Triple(0xFF7A5D00.toInt(),0xFFFFE28A.toInt(),0xFFFFF8E0.toInt()),
    "سايبر" to Triple(0xFF005A9C.toInt(),0xFF8FE8FF.toInt(),0xFFE8F8FF.toInt()),
    "نيون" to Triple(0xFF5B21B6.toInt(),0xFFFF2D95.toInt(),0xFF0B1020.toInt()),
    "ليل" to Triple(0xFF171C35.toInt(),0xFF6B4EFF.toInt(),0xFF090B14.toInt()),
    "مرجان" to Triple(0xFFE11D48.toInt(),0xFFFFC4D6.toInt(),0xFFFFF1F5.toInt()),
    "فيروز" to Triple(0xFF00897B.toInt(),0xFF80CBC4.toInt(),0xFFE0F7F4.toInt()),
    "ثلج" to Triple(0xFF456A8A.toInt(),0xFFDDEEFF.toInt(),0xFFF7FBFF.toInt()),
    "لافندر" to Triple(0xFF7C4DFF.toInt(),0xFFE1D7FF.toInt(),0xFFF7F4FF.toInt()),
    "ليمون" to Triple(0xFF6A7B00.toInt(),0xFFE7F58A.toInt(),0xFFFAFDE6.toInt()),
    "مانجو" to Triple(0xFFF57C00.toInt(),0xFFFFD180.toInt(),0xFFFFF5E6.toInt()),
    "توت" to Triple(0xFF7B1FA2.toInt(),0xFFE1BEE7.toInt(),0xFFF9F0FB.toInt()),
    "أزرق ملكي" to Triple(0xFF304FFE.toInt(),0xFF9FA8DA.toInt(),0xFFF0F2FF.toInt()),
    "أخضر زمردي" to Triple(0xFF00695C.toInt(),0xFF80CBC4.toInt(),0xFFE8F7F4.toInt()),
    "فحم" to Triple(0xFF212121.toInt(),0xFF616161.toInt(),0xFF111111.toInt()),
    "فضي" to Triple(0xFF546E7A.toInt(),0xFFECEFF1.toInt(),0xFFF8FAFB.toInt()),
    "ذهبي" to Triple(0xFF8D6E00.toInt(),0xFFFFE082.toInt(),0xFFFFFAE8.toInt()),
    "نحاسي" to Triple(0xFF8D4A2F.toInt(),0xFFE0A080.toInt(),0xFFFFF1EA.toInt()),
    "بنفسجي داكن" to Triple(0xFF4527A0.toInt(),0xFF7E57C2.toInt(),0xFF100A25.toInt()),
    "أحمر داكن" to Triple(0xFF8B1E2D.toInt(),0xFFE57373.toInt(),0xFF21070B.toInt()),
    "أخضر داكن" to Triple(0xFF1B5E20.toInt(),0xFF66BB6A.toInt(),0xFF071A09.toInt()),
    "أزرق داكن" to Triple(0xFF0D47A1.toInt(),0xFF42A5F5.toInt(),0xFF061225.toInt()),
    "وردي فاتح" to Triple(0xFFAD1457.toInt(),0xFFF8BBD0.toInt(),0xFFFFF4F8.toInt()),
    "أزرق سماوي" to Triple(0xFF0277BD.toInt(),0xFF81D4FA.toInt(),0xFFEAF9FF.toInt()),
    "نعناعي" to Triple(0xFF00897B.toInt(),0xFFA7FFEB.toInt(),0xFFEFFFFB.toInt()),
    "خوخي" to Triple(0xFFBF5B32.toInt(),0xFFFFCCBC.toInt(),0xFFFFF4F0.toInt()),
    "أوركيد" to Triple(0xFF8E24AA.toInt(),0xFFCE93D8.toInt(),0xFFFCEFFF.toInt()),
    "مجرة" to Triple(0xFF311B92.toInt(),0xFF00BCD4.toInt(),0xFF09051A.toInt()),
    "نار" to Triple(0xFFD84315.toInt(),0xFFFFB300.toInt(),0xFF200604.toInt()),
    "محيط ليلي" to Triple(0xFF01579B.toInt(),0xFF00ACC1.toInt(),0xFF03131B.toInt()),
    "غروب وردي" to Triple(0xFFC2185B.toInt(),0xFFFF8A65.toInt(),0xFFFFEEF4.toInt()),
    "ربيع" to Triple(0xFF558B2F.toInt(),0xFFDCE775.toInt(),0xFFF7FBEA.toInt())
)

private data class ShapeSpec(val id:Int,val name:String,val family:Int,val variant:Int,val sides:Int,val round:Float,val skew:Float,val notch:Float,val rotation:Float,val threeD:Boolean,val depth:Float)
private val shapeFamilies = listOf("Rounded Square","Soft Square","Circle","Pill","Capsule","Hexagon","Octagon","Diamond","Triangle","Pentagon","Heptagon","Nonagon","Decagon","Star","Burst","Gear","Leaf","Shield","Ticket","Cloud","Arch","Cut Corner","Chamfer","Parallelogram","Blob","Trapezoid","Arrow","Heart","Petal","Wave","Superellipse","Cross")

/* 1024 procedural, deterministic shapes: 768 2D + 256 3D. Variants change geometry, rotation, skew and notches. */
private val shapeLibrary: List<ShapeSpec> by lazy {
    val out=ArrayList<ShapeSpec>(1024);var id=0
    shapeFamilies.forEachIndexed { family,_ -> repeat(24) { v ->
        val sides=when(family){2->64;5->6;6->8;7->4;8->3;9->5;10->7;11->9;12->10;else->8}
        out += ShapeSpec(id++,"${shapeFamilies[family]} ${v+1}",family,v,sides,(v%12)/11f,((v%9)-4)/20f,(v%11)/11f,(v*15f)%360f,false,0f)
    }}
    val d3Families=listOf(0,1,2,5,6,7,8,13,14,15,17,18,20,21,22,30)
    d3Families.forEach { family -> repeat(16) { v ->
        out += ShapeSpec(id++,"${shapeFamilies[family]} 3D ${v+1}",family,v,8,(v%12)/11f,((v%9)-4)/20f,(v%11)/11f,(v*23f)%360f,true,5f+(v%6)*2f)
    }}
    out
}

data class BuiltInFont(val id:String,val name:String,val language:String,val assetPath:String)
private fun bundledFonts(context:Context):List<BuiltInFont>{
    val names=runCatching{context.assets.list("fonts")?.filter{it.endsWith(".ttf",true)||it.endsWith(".otf",true)}?:emptyList()}.getOrDefault(emptyList())
    return names.sorted().mapIndexed{index,n->BuiltInFont("bundled_$index",n.substringBeforeLast('.'),if(n.contains("Arabic",true)||n.contains("KufiArabic",true)||n.contains("NaskhArabic",true))"العربية" else "English","fonts/$n")}
}

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{FutoStudioApp()}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun FutoStudioApp(){
    val context=LocalContext.current
    var theme by remember{mutableStateOf(ThemeState())};var tab by remember{mutableIntStateOf(0)};var status by remember{mutableStateOf("جاهز — ${shapeLibrary.size} شكل هندسي")};var imageTargetGroup by remember{mutableStateOf<KeyGroup?>(null)}
    val importZip=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{theme=readTheme(context,uri);status="تم استيراد ثيم FUTO"}.onFailure{status="فشل الاستيراد: ${it.message}"}}
    val bgPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{val b=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};theme=theme.copy(backgroundImage=b,backgroundFileName="background.png");status="تم تعيين خلفية الكيبورد"}.onFailure{status="فشل اختيار الخلفية"}}
    val keyPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{val b=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};val k=theme.selectedKey;theme=theme.copy(keyImages=theme.keyImages+(k to b),keyImageNames=theme.keyImageNames+(k to "Key-${safe(k)}.png"));status="تم تعيين صورة للزر $k"}.onFailure{status="فشل اختيار صورة الزر"}}
    val groupPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{val b=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};val g=imageTargetGroup?:theme.selectedGroup;val key="GROUP_${g.name}";val s=theme.groupStyles[g]?:GroupStyle();theme=theme.updateGroup(g,s.copy(imageKey=key)).copy(keyImages=theme.keyImages+(key to b),keyImageNames=theme.keyImageNames+(key to "Group-${g.name}.png"));status="تم تعيين صورة لمجموعة ${g.title}"}.onFailure{status="فشل اختيار صورة المجموعة"}}
    val fontPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{val b=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};val n=(uri.lastPathSegment?:"CustomFont.ttf").substringAfterLast('/');val clean=if(n.endsWith(".ttf",true)||n.endsWith(".otf",true))n else "$n.ttf";if(theme.exportFontLanguage=="العربية")theme=theme.copy(arabicFontBytes=b,arabicFontName=clean)else theme=theme.copy(englishFontBytes=b,englishFontName=clean);status="تم استيراد الخط للغة ${theme.exportFontLanguage}"}.onFailure{status="فشل استيراد الخط"}}
    val exportPicker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){uri->if(uri!=null)runCatching{validateTheme(theme);exportThemeToUri(context,theme,uri);status="تم تصدير ZIP متوافق مع صيغة FUTO"}.onFailure{status="فشل التصدير: ${it.message}"}}
    val dark=theme.light.not();val colors=if(dark)darkColorScheme(primary=Color(theme.primary),background=Color(0xFF121218),surface=Color(0xFF1D1B20))else lightColorScheme(primary=Color(theme.primary),background=Color(theme.background),surface=Color(theme.background))
    MaterialTheme(colorScheme=colors){Scaffold(topBar={Column{TopAppBar(title={Text("FUTO Theme Studio",fontWeight=FontWeight.Bold)},navigationIcon={Icon(Icons.Default.Keyboard,null)},actions={IconButton({importZip.launch(arrayOf("application/zip","application/octet-stream"))}){Icon(Icons.Default.FileOpen,"استيراد ZIP")};IconButton({exportPicker.launch("${safeFile(theme.name)}.zip")}){Icon(Icons.Default.Archive,"تصدير ZIP")}});NavigationStrip(tab){tab=it}}},bottomBar={Text(status,Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),style=MaterialTheme.typography.labelSmall)},containerColor=MaterialTheme.colorScheme.background){pad->Box(Modifier.fillMaxSize().padding(pad)){when(tab){0->PreviewStudio(theme,{theme=it},bgPicker,keyPicker,groupPicker,fontPicker,{imageTargetGroup=it});1->LayoutScreen(theme){theme=it};2->ShapesScreen(theme){theme=it};3->ColorsScreen(theme){theme=it};4->AssetsScreen(theme,bgPicker,keyPicker,groupPicker,{imageTargetGroup=it},fontPicker){theme=it};5->FontsScreen(theme,fontPicker){theme=it};6->ExportScreen(theme){exportPicker.launch("${safeFile(theme.name)}.zip")};7->SettingsScreen(theme){theme=it}}}}}
}

@Composable private fun NavigationStrip(selected:Int,on:(Int)->Unit){
    val items=listOf("المعاينة","التخطيط","الأشكال","الألوان","الصور","الخطوط","التصدير","الإعدادات")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=8.dp,vertical=5.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        items.forEachIndexed { i,n ->
            FilterChip(selected=i==selected,onClick={on(i)},label={Text(n)},leadingIcon={if(i==selected)Icon(Icons.Default.Check,null,Modifier.size(16.dp))})
        }
    }
}

@Composable private fun PreviewStudio(t:ThemeState,onChange:(ThemeState)->Unit,bgPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,keyPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,groupPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,fontPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,selectGroup:(KeyGroup)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text("المعاينة الحية",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        Text("هذه المعاينة مرتبطة مباشرة بإعدادات الثيم والتصدير.",style=MaterialTheme.typography.bodySmall)
        TopFunctionPreview(t){onChange(t.copy(selectedKey=it))}
        Text("عربي",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);KeyboardPreview(t,arabicRows){onChange(t.copy(selectedKey=it))}
        Text("English",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);KeyboardPreview(t,englishRows){onChange(t.copy(selectedKey=it))}
        SectionCard("تخطيط الأزرار"){
            GroupChips(t){onChange(t.copy(selectedGroup=it))}
            Text("المجموعة الحالية: ${t.selectedGroup.title}")
            Slider(value=t.buttonScale,onValueChange={onChange(t.copy(buttonScale=it))},valueRange=.75f..1.3f);Text("حجم الأزرار: ${String.format(Locale.US,"%.2f",t.buttonScale)}×")
            Slider(value=t.spacing,onValueChange={onChange(t.copy(spacing=it))},valueRange=1f..10f);Text("المسافة: ${String.format(Locale.US,"%.1f",t.spacing)}dp")
        }
        SectionCard("الأشكال") { ShapeQuickRow(t){onChange(it)}; Text("${shapeLibrary.size} شكل هندسي متاح، منها 256 شكل 3D.",style=MaterialTheme.typography.bodySmall) }
        SectionCard("الخطوط") { FontLanguageChips(t){onChange(it)};Text("العربي والإنجليزي منفصلان في المعاينة؛ تغيير أحدهما لا يغيّر الآخر.",style=MaterialTheme.typography.bodySmall);Button({fontPicker.launch(arrayOf("font/ttf","font/otf","application/octet-stream"))},Modifier.fillMaxWidth()){Icon(Icons.Default.FontDownload,null);Spacer(Modifier.width(6.dp));Text("إضافة خط من الهاتف")}}
        SectionCard("الألوان") {PaletteRow(t){onChange(it)};Text("أحادي، متدرج، فاتح، داكن، وخلفية صورة.",style=MaterialTheme.typography.bodySmall)}
        SectionCard("الصور") {Button({bgPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("اختيار خلفية الكيبورد")};Button({keyPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("صورة للزر المحدد: ${t.selectedKey}")};Button({selectGroup(t.selectedGroup);groupPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("صورة لمجموعة ${t.selectedGroup.title}")}}
    }
}

@Composable private fun LayoutScreen(t:ThemeState,onChange:(ThemeState)->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){Text("تخطيط الأزرار",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("حدد المجموعة ثم عدّل شكلها أو اجعل لزر محدد إعدادًا خاصًا.",style=MaterialTheme.typography.bodySmall);GroupChips(t){onChange(t.copy(selectedGroup=it))};Divider(Modifier.padding(vertical=8.dp));Text("الزر المحدد: ${t.selectedKey}",style=MaterialTheme.typography.titleLarge);Slider(value=t.arabicTextScale,onValueChange={onChange(t.copy(arabicTextScale=it))},valueRange=.7f..1.5f);Text("حجم الحروف العربية: ${String.format(Locale.US,"%.2f",t.arabicTextScale)}×");Slider(value=t.englishTextScale,onValueChange={onChange(t.copy(englishTextScale=it))},valueRange=.7f..1.5f);Text("حجم الحروف الإنجليزية: ${String.format(Locale.US,"%.2f",t.englishTextScale)}×");Slider(value=t.radius,onValueChange={onChange(t.copy(radius=it))},valueRange=4f..50f);Text("استدارة عامة: ${String.format(Locale.US,"%.0f",t.radius)}")}}

@Composable private fun ShapesScreen(t:ThemeState,onChange:(ThemeState)->Unit){
    var only3D by remember{mutableStateOf(false)};var query by remember{mutableStateOf("")};var perKey by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(10.dp)){
        Text("مكتبة الأشكال",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);GroupChips(t){onChange(t.copy(selectedGroup=it))}
        Row(verticalAlignment=Alignment.CenterVertically){FilterChip(!perKey,{perKey=false},label={Text("المجموعة")});FilterChip(perKey,{perKey=true},label={Text("الزر ${t.selectedKey}")});Spacer(Modifier.weight(1f));FilterChip(only3D,{only3D=!only3D},label={Text("3D فقط")})}
        Text("${shapeLibrary.size} شكل • ${shapeLibrary.count{it.threeD}} ثلاثي الأبعاد",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(query,{query=it},label={Text("بحث في الأشكال")},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=6.dp))
        val list=remember(only3D,query){shapeLibrary.asSequence().filter{!only3D||it.threeD}.filter{query.isBlank()||it.name.contains(query,true)}.toList()}
        LazyVerticalGrid(columns=GridCells.Adaptive(100.dp),modifier=Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            items(list,key={it.id}){spec->val current=if(perKey)t.keyOverrides[t.selectedKey]?.shapeId else t.groupStyles[t.selectedGroup]?.shapeId;ShapeTile(spec,current==spec.id){val base=t.groupStyles[t.selectedGroup]?:GroupStyle();onChange(if(perKey)t.copy(keyOverrides=t.keyOverrides+(t.selectedKey to (t.keyOverrides[t.selectedKey]?:base).copy(shapeId=spec.id)))else t.updateGroup(t.selectedGroup,base.copy(shapeId=spec.id)))}}
        }
    }
}

@Composable private fun ShapeQuickRow(t:ThemeState,onChange:(ThemeState)->Unit){val current=t.groupStyles[t.selectedGroup]?.shapeId?:0;Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){shapeLibrary.filter{it.id<18}.forEach{spec->ShapeTile(spec,current==spec.id,Modifier.width(82.dp)){onChange(t.updateGroup(t.selectedGroup,(t.groupStyles[t.selectedGroup]?:GroupStyle()).copy(shapeId=spec.id)))}}}}
@Composable private fun ShapeTile(spec:ShapeSpec,selected:Boolean,onClick:()->Unit)=ShapeTile(spec,selected,Modifier.width(100.dp),onClick)
@Composable private fun ShapeTile(spec:ShapeSpec,selected:Boolean,modifier:Modifier,onClick:()->Unit){Column(modifier.clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally){ShapeSwatch(spec,selected);Text(spec.name,style=MaterialTheme.typography.labelSmall,maxLines=1)}}
@Composable private fun ShapeSwatch(spec:ShapeSpec,selected:Boolean){val shape=composeShape(spec);Box(Modifier.size(62.dp,48.dp).clip(shape).background(if(spec.threeD)Brush.linearGradient(listOf(Color(0xFF10131D),Color(0xFF7C5CFF),Color(0xFF1AE1C1))) else Brush.linearGradient(listOf(Color(0xFF4F46E5),Color(0xFFEC4899)))).border(if(selected)2.dp else 1.dp,if(selected)MaterialTheme.colorScheme.primary else Color.White.copy(alpha=.2f),shape))}

@Composable private fun ColorsScreen(t:ThemeState,onChange:(ThemeState)->Unit){
    val scope=rememberCoroutineScope();var url by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var onlineStatus by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){
        Text("الألوان",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("مجموعات مستقلة للزر والخلفية، مع جاهزات فاتحة وداكنة وحية.",style=MaterialTheme.typography.bodySmall);PaletteRow(t,onChange)
        SectionCard("لوحة ألوان من الإنترنت"){
            OutlinedTextField(url,{url=it},label={Text("رابط صفحة أو ملف يحتوي ألوان HEX")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Button(enabled=!loading&&url.isNotBlank(),onClick={loading=true;onlineStatus="جاري التحميل…";scope.launch(Dispatchers.IO){runCatching{val text=URL(url).openStream().bufferedReader().use{it.readText()};val cs=Regex("#[0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?").findAll(text).mapNotNull{parseColor(it.value)}.distinct().take(2).toList();if(cs.isEmpty())error("لم نجد ألوان HEX");withContext(Dispatchers.Main){onChange(t.copy(background=cs[0],background2=cs.getOrElse(1){lighten(cs[0])},backgroundMode=FillMode.GRADIENT,primary=cs[0]));onlineStatus="تم استيراد ${cs.size} ألوان من الرابط"}}.onFailure{withContext(Dispatchers.Main){onlineStatus="تعذر تحميل الألوان: ${it.message}"}};loading=false}},Modifier.fillMaxWidth()){Text(if(loading)"تحميل…" else "تحميل الألوان من الرابط")};if(onlineStatus.isNotBlank())Text(onlineStatus,style=MaterialTheme.typography.bodySmall)
        }
        Divider(Modifier.padding(vertical=10.dp));Text("خلفية الكيبورد",style=MaterialTheme.typography.titleLarge);ColorField("الخلفية الأولى",t.background){onChange(t.copy(background=it))};ColorField("الخلفية الثانية",t.background2){onChange(t.copy(background2=it))};SwitchRow("تدرج الخلفية",t.backgroundMode==FillMode.GRADIENT){onChange(t.copy(backgroundMode=if(it)FillMode.GRADIENT else FillMode.SOLID))};Divider(Modifier.padding(vertical=10.dp));GroupChips(t){onChange(t.copy(selectedGroup=it))};GroupColorEditor(t,t.selectedGroup,onChange)
    }
}
@Composable private fun PaletteRow(t:ThemeState,onChange:(ThemeState)->Unit){Column{Text("ألوان جاهزة",style=MaterialTheme.typography.titleMedium);Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){palettes.forEach{(n,c)->FilterChip(false,{onChange(applyPalette(t,c))},label={Text(n)})}}}}
@Composable private fun GroupColorEditor(t:ThemeState,g:KeyGroup,onChange:(ThemeState)->Unit){val s=t.groupStyles[g]?:GroupStyle();Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text("ألوان ${g.title}",style=MaterialTheme.typography.titleLarge);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(s.fillMode==FillMode.SOLID,{onChange(t.updateGroup(g,s.copy(fillMode=FillMode.SOLID)))},label={Text("أحادي")});FilterChip(s.fillMode==FillMode.GRADIENT,{onChange(t.updateGroup(g,s.copy(fillMode=FillMode.GRADIENT)))},label={Text("متدرج")})};ColorField("اللون الأول",s.color1){onChange(t.updateGroup(g,s.copy(color1=it)))};if(s.fillMode==FillMode.GRADIENT)ColorField("اللون الثاني",s.color2){onChange(t.updateGroup(g,s.copy(color2=it)))};ColorField("لون النص",s.textColor){onChange(t.updateGroup(g,s.copy(textColor=it)))}}}
@Composable private fun ColorField(title:String,value:Int,on:(Int)->Unit){var text by remember(value){mutableStateOf("#"+String.format(Locale.US,"%08X",value))};Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(30.dp).clip(CircleShape).background(Color(value)));Spacer(Modifier.width(8.dp));OutlinedTextField(text,{text=it;parseColor(it)?.let(on)},label={Text(title)},singleLine=true,modifier=Modifier.weight(1f))}}
@Composable private fun SwitchRow(title:String,checked:Boolean,on:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(title);Switch(checked,on)}}

@Composable private fun AssetsScreen(t:ThemeState,bgPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,keyPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,groupPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,selectGroup:(KeyGroup)->Unit,fontPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,onChange:(ThemeState)->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){Text("الصور",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Button({bgPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("خلفية الشاشة")};Button({keyPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("صورة للزر ${t.selectedKey}")};GroupChips(t){onChange(t.copy(selectedGroup=it))};Button({selectGroup(t.selectedGroup);groupPicker.launch(arrayOf("image/*"))},Modifier.fillMaxWidth()){Text("صورة لمجموعة ${t.selectedGroup.title}")};Divider(Modifier.padding(vertical=8.dp));Text("الصور المخصصة: ${t.keyImages.size}")}}

@Composable private fun FontsScreen(t:ThemeState,fontPicker:androidx.activity.compose.ManagedActivityResultLauncher<Array<String>,Uri?>,onChange:(ThemeState)->Unit){
    val context=LocalContext.current;val fonts=bundledFonts(context);val arabic=t.exportFontLanguage=="العربية";val selectedBytes=if(arabic)t.arabicFontBytes else t.englishFontBytes;val selectedName=if(arabic)t.arabicFontName else t.englishFontName
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){
        Text("الخطوط",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(arabic,{onChange(t.copy(exportFontLanguage="العربية"))},label={Text("العربية")});FilterChip(!arabic,{onChange(t.copy(exportFontLanguage="English"))},label={Text("English")})}
        SectionCard("المعاينة قبل الإضافة"){Text(selectedName?:"لم يتم اختيار خط بعد",style=MaterialTheme.typography.titleMedium);Text(if(arabic)"أبجد هوز حطي كلمن — العربية 12345" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ — abcdefgh 12345",fontSize=26.sp,fontFamily=fontFamilyForBytes(context,selectedBytes),fontWeight=FontWeight.Normal)}
        Text("العربية: ${fonts.count{it.language=="العربية"}} • English: ${fonts.count{it.language=="English"}}",style=MaterialTheme.typography.bodySmall)
        fonts.filter{it.language==t.exportFontLanguage}.forEach{f->ElevatedCard(onClick={val b=runCatching{context.assets.open(f.assetPath).use{it.readBytes()}}.getOrNull();if(b!=null){if(f.language=="العربية")onChange(t.copy(arabicFontName=f.name,arabicFontBytes=b))else onChange(t.copy(englishFontName=f.name,englishFontBytes=b))}},modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(f.name,fontWeight=FontWeight.SemiBold);Text(if(f.language=="العربية")"خط عربي" else "Latin / English",style=MaterialTheme.typography.labelSmall)};Icon(Icons.Default.FontDownload,null)}}}
        Button({fontPicker.launch(arrayOf("font/ttf","font/otf","application/octet-stream"))},Modifier.fillMaxWidth()){Text("استيراد خط TTF / OTF للغة المحددة")}
        Text("المعاينة تفصل العربية عن الإنجليزية. صيغة FUTO الحالية تحتوي خانة font عامة واحدة، لذلك يحدد خيار لغة التصدير أي خط يوضع في theme.txt؛ الخط الآخر لا يُفرض على اللغة الأخرى في المعاينة.",style=MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun ExportScreen(t:ThemeState,onExport:()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){Text("التصدير",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("سيتم إنشاء ZIP حقيقي مع theme.txt والأصول والصور والخط المختار.",style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(10.dp));Text("اسم الملف: ${safeFile(t.name)}.zip");Text("الشكل المحدد: ${t.groupStyles[t.selectedGroup]?.shapeId?:0} • ${shapeLibrary.size} شكل متاح");Text("خط التصدير: ${t.exportFontLanguage}");Button(onExport,Modifier.fillMaxWidth().height(54.dp)){Icon(Icons.Default.Archive,null);Spacer(Modifier.width(8.dp));Text("تصدير ZIP")};Text("التحقق يمنع كتابة أرقام محلية مثل ٠٫٣٦٧ داخل TOML.",style=MaterialTheme.typography.bodySmall)}}

@Composable private fun SettingsScreen(t:ThemeState,onChange:(ThemeState)->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){Text("إعدادات الثيم",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Edit("اسم الثيم",t.name){onChange(t.copy(name=it))};Edit("المؤلف",t.author){onChange(t.copy(author=it))};Edit("ID",t.id){onChange(t.copy(id=it))};Edit("الوصف",t.description){onChange(t.copy(description=it))};SwitchRow("الوضع الداكن",!t.light){onChange(t.copy(light=!it))}}}
@Composable private fun Edit(label:String,value:String,on:(String)->Unit){OutlinedTextField(value,onValueChange=on,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=4.dp))}

@Composable private fun GroupChips(t:ThemeState,on:(KeyGroup)->Unit){Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){KeyGroup.values().forEach{g->FilterChip(t.selectedGroup==g,{on(g)},label={Text(g.title)})}}}
@Composable private fun TopFunctionPreview(t:ThemeState,onChange:(String)->Unit){val s=t.groupStyles[KeyGroup.TOP]?:GroupStyle();Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(backgroundBrush(t)).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){topBar.forEach{k->PreviewKey(t,k,s,KeyGroup.TOP,Modifier.weight(1f).height(42.dp),onChange)}}}
@Composable private fun KeyboardPreview(t:ThemeState,rows:List<List<String>>,onChange:(String)->Unit){Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(backgroundBrush(t)).padding(8.dp),verticalArrangement=Arrangement.spacedBy(t.spacing.dp)){rows.forEachIndexed{ri,row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(t.spacing.dp)){row.forEach{label->val g=groupFor(label,ri,ri==rows.lastIndex);val style=t.keyOverrides[label]?:t.groupStyles[g]?:GroupStyle();val w=if(label=="مسافة"||label=="SPACE")2.7f else if(label=="123")1.2f else 1f;PreviewKey(t,label,style,g,Modifier.weight(w).height((48f*t.buttonScale).dp),onChange)}}}}}
@Composable private fun PreviewKey(t:ThemeState,label:String,style:GroupStyle,group:KeyGroup,modifier:Modifier,onChange:(String)->Unit){val context=LocalContext.current;val shape=composeShape(shapeLibrary.getOrElse(style.shapeId){shapeLibrary[0]});val imageBytes=t.keyImages[label]?:style.imageKey?.let{t.keyImages[it]};val bitmap=remember(imageBytes){imageBytes?.let{BitmapFactory.decodeByteArray(it,0,it.size)}};Box(modifier.clip(shape).background(if(style.fillMode==FillMode.GRADIENT)Brush.linearGradient(listOf(Color(style.color1).copy(alpha=style.opacity),Color(style.color2).copy(alpha=style.opacity))) else Brush.verticalGradient(listOf(Color(style.color1).copy(alpha=style.opacity),Color(style.color1).copy(alpha=style.opacity)))).border(if(t.selectedKey==label)2.dp else t.borderWidth.dp,if(t.selectedKey==label)MaterialTheme.colorScheme.primary else Color(t.borderColor),shape).clickable{onChange(label)},contentAlignment=Alignment.Center){bitmap?.let{Image(it.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)};val arabic=label.any{it in '\u0600'..'\u06ff'};val scale=if(label=="مسافة"||label=="SPACE") .72f else if(arabic)t.arabicTextScale else t.englishTextScale;Text(label,fontSize=(18f*scale).sp,color=Color(style.textColor),fontFamily=fontFamily(t,context,arabic),maxLines=1)}}
@Composable private fun fontFamily(t:ThemeState,context:Context,arabic:Boolean):FontFamily=fontFamilyForBytes(context,if(arabic)t.arabicFontBytes else t.englishFontBytes)
@Composable private fun fontFamilyForBytes(context:Context,bytes:ByteArray?):FontFamily=remember(bytes){if(bytes==null)FontFamily.Default else runCatching{val f=File(context.cacheDir,"preview-${bytes.contentHashCode()}.font");if(!f.exists())f.writeBytes(bytes);FontFamily(Typeface.createFromFile(f))}.getOrDefault(FontFamily.Default)}
@Composable private fun backgroundBrush(t:ThemeState):Brush=if(t.backgroundMode==FillMode.GRADIENT)Brush.linearGradient(listOf(Color(t.background),Color(t.background2))) else Brush.verticalGradient(listOf(Color(t.background),Color(t.background)))
@Composable private fun SectionCard(title:String,content:@Composable ColumnScope.()->Unit){ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);content()}}}

private fun ThemeState.updateGroup(g:KeyGroup,s:GroupStyle)=copy(groupStyles=groupStyles.toMutableMap().apply{put(g,s)})
private fun groupFor(label:String,row:Int,lastRow:Boolean):KeyGroup=when{label=="مسافة"||label=="SPACE"->KeyGroup.SPACE;label=="↵"->KeyGroup.ACTION;label=="⌫"||label=="⇧"||label=="123"||label=="🌐"->KeyGroup.FUNCTIONAL;label in topBar->KeyGroup.TOP;lastRow->KeyGroup.BOTTOM;else->KeyGroup.LETTERS}
private fun applyPalette(t:ThemeState,c:Triple<Int,Int,Int>):ThemeState{val m=t.groupStyles.toMutableMap();m[KeyGroup.LETTERS]=m[KeyGroup.LETTERS]!!.copy(color1=c.second,color2=lighten(c.second),fillMode=FillMode.GRADIENT);m[KeyGroup.TOP]=m[KeyGroup.TOP]!!.copy(color1=c.first,color2=lighten(c.first),fillMode=FillMode.GRADIENT);m[KeyGroup.BOTTOM]=m[KeyGroup.BOTTOM]!!.copy(color1=c.second,color2=c.third,fillMode=FillMode.GRADIENT);m[KeyGroup.ACTION]=m[KeyGroup.ACTION]!!.copy(color1=c.first,color2=lighten(c.first),fillMode=FillMode.GRADIENT);return t.copy(primary=c.first,background=c.third,background2=lighten(c.third),backgroundMode=FillMode.GRADIENT,groupStyles=m)}

private fun composeShape(s:ShapeSpec):androidx.compose.ui.graphics.Shape=when(s.family){0,1,21,22->RoundedCornerShape((6+s.round*32).dp);2->CircleShape;3,4->RoundedCornerShape(50);else->object:androidx.compose.ui.graphics.Shape{override fun createOutline(size:androidx.compose.ui.geometry.Size,layoutDirection:androidx.compose.ui.unit.LayoutDirection,density:androidx.compose.ui.unit.Density)=androidx.compose.ui.graphics.Outline.Generic(pathForShape(s,size.width,size.height).asComposePath())}}

private fun pathForShape(s: ShapeSpec, w: Float, h: Float): AndroidPath {
    val p = AndroidPath()
    val cx = w * 0.5f
    val cy = h * 0.5f
    val rx = w * 0.46f
    val ry = h * 0.40f
    val rotation = s.rotation

    fun poly(n: Int, inner: Float = 1.0f) {
        for (i in 0 until n) {
            val angle = Math.toRadians(rotation.toDouble() + i.toDouble() * 360.0 / n.toDouble())
            val radius = if (i % 2 == 0) 1.0f else inner * (1.0f - s.notch * 0.18f)
            val cosine = cos(angle)
            val sine = sin(angle)
            val x = cx + rx * radius * cosine.toFloat() + s.skew * w * 0.15f * (i % 2).toFloat()
            val y = cy + ry * radius * sine.toFloat()
            if (i == 0) {
                p.moveTo(x, y)
            } else {
                p.lineTo(x, y)
            }
        }
        p.close()
    }

    when (s.family) {
        0 -> {
            val radius = 10.0f + s.round * 42.0f
            p.addRoundRect(8.0f, 8.0f, w - 8.0f, h - 8.0f, radius, radius, AndroidPath.Direction.CW)
        }
        1 -> {
            val radius = 22.0f + s.round * 30.0f
            p.addRoundRect(6.0f, 6.0f, w - 6.0f, h - 6.0f, radius, radius, AndroidPath.Direction.CW)
        }
        2 -> {
            p.addCircle(cx, cy, min(rx, ry), AndroidPath.Direction.CW)
        }
        3 -> {
            p.addRoundRect(4.0f, 22.0f, w - 4.0f, h - 22.0f, 28.0f, 28.0f, AndroidPath.Direction.CW)
        }
        4 -> {
            p.addRoundRect(7.0f, 13.0f, w - 7.0f, h - 13.0f, 42.0f, 42.0f, AndroidPath.Direction.CW)
        }
        5 -> poly(6)
        6 -> poly(8)
        7 -> poly(4)
        8 -> poly(3)
        9 -> poly(5)
        10 -> poly(7)
        11 -> poly(9)
        12 -> poly(10)
        13 -> poly(10, 0.45f)
        14 -> poly(18, 0.72f)
        15 -> poly(16, 0.78f)
        16 -> {
            p.moveTo(cx, 5.0f)
            p.cubicTo(w * 0.88f, h * 0.25f, w * 0.72f, h * 0.84f, cx, h - 5.0f)
            p.cubicTo(w * 0.28f, h * 0.84f, w * 0.12f, h * 0.25f, cx, 5.0f)
            p.close()
        }
        17 -> {
            p.moveTo(cx, 5.0f)
            p.lineTo(w * 0.88f, h * 0.25f)
            p.lineTo(w * 0.76f, h * 0.75f)
            p.quadTo(cx, h - 5.0f, cx, h - 5.0f)
            p.quadTo(w * 0.24f, h * 0.75f, w * 0.12f, h * 0.25f)
            p.close()
        }
        18 -> {
            p.moveTo(8.0f, 10.0f)
            p.lineTo(w - 8.0f, 10.0f)
            p.lineTo(w - 20.0f, cy)
            p.lineTo(w - 8.0f, h - 10.0f)
            p.lineTo(8.0f, h - 10.0f)
            p.lineTo(20.0f, cy)
            p.close()
        }
        19 -> {
            p.moveTo(w * 0.12f, h * 0.66f)
            p.cubicTo(w * 0.08f, h * 0.40f, w * 0.25f, h * 0.18f, w * 0.43f, h * 0.34f)
            p.cubicTo(w * 0.50f, h * 0.08f, w * 0.78f, h * 0.16f, w * 0.75f, h * 0.38f)
            p.cubicTo(w * 0.94f, h * 0.34f, w * 0.96f, h * 0.68f, w * 0.76f, h * 0.70f)
            p.cubicTo(w * 0.56f, h * 0.92f, w * 0.28f, h * 0.86f, w * 0.12f, h * 0.66f)
            p.close()
        }
        20 -> {
            p.moveTo(w * 0.12f, h * 0.78f)
            p.lineTo(w * 0.12f, h * 0.42f)
            p.cubicTo(w * 0.18f, h * 0.08f, w * 0.82f, h * 0.08f, w * 0.88f, h * 0.42f)
            p.lineTo(w * 0.88f, h * 0.78f)
            p.close()
        }
        21 -> {
            p.moveTo(22.0f, 6.0f)
            p.lineTo(w - 22.0f, 6.0f)
            p.lineTo(w - 6.0f, 22.0f)
            p.lineTo(w - 6.0f, h - 22.0f)
            p.lineTo(w - 22.0f, h - 6.0f)
            p.lineTo(22.0f, h - 6.0f)
            p.lineTo(6.0f, h - 22.0f)
            p.lineTo(6.0f, 22.0f)
            p.close()
        }
        22 -> {
            p.moveTo(22.0f, 6.0f)
            p.lineTo(w - 22.0f, 6.0f)
            p.lineTo(w - 6.0f, 22.0f)
            p.lineTo(w - 6.0f, h - 6.0f)
            p.lineTo(6.0f, h - 6.0f)
            p.lineTo(6.0f, 22.0f)
            p.close()
        }
        23 -> {
            val skew = s.skew * w * 0.25f
            p.moveTo(8.0f + skew, 8.0f)
            p.lineTo(w - 8.0f + skew, 8.0f)
            p.lineTo(w - 8.0f - skew, h - 8.0f)
            p.lineTo(8.0f - skew, h - 8.0f)
            p.close()
        }
        24 -> {
            p.moveTo(cx, 6.0f)
            p.cubicTo(w * 0.80f, 6.0f, w - 6.0f, h * 0.25f, w * 0.78f, h * 0.42f)
            p.cubicTo(w - 8.0f, h * 0.72f, w * 0.68f, h - 6.0f, cx, h - 6.0f)
            p.cubicTo(w * 0.32f, h - 6.0f, 8.0f, h * 0.72f, w * 0.22f, h * 0.42f)
            p.cubicTo(6.0f, h * 0.25f, w * 0.20f, 6.0f, cx, 6.0f)
            p.close()
        }
        25 -> {
            p.moveTo(w * 0.22f, 6.0f)
            p.lineTo(w * 0.78f, 6.0f)
            p.lineTo(w - 6.0f, h - 6.0f)
            p.lineTo(6.0f, h - 6.0f)
            p.close()
        }
        26 -> {
            p.moveTo(w * 0.08f, cy)
            p.lineTo(w * 0.58f, 6.0f)
            p.lineTo(w * 0.58f, h * 0.28f)
            p.lineTo(w * 0.92f, h * 0.28f)
            p.lineTo(w * 0.92f, h * 0.72f)
            p.lineTo(w * 0.58f, h * 0.72f)
            p.lineTo(w * 0.58f, h - 6.0f)
            p.close()
        }
        27 -> {
            p.moveTo(cx, h - 6.0f)
            p.cubicTo(w * 0.12f, h * 0.58f, w * 0.08f, h * 0.28f, w * 0.30f, h * 0.18f)
            p.cubicTo(w * 0.44f, h * 0.12f, cx, h * 0.26f, cx, h * 0.26f)
            p.cubicTo(cx, h * 0.26f, w * 0.56f, h * 0.12f, w * 0.70f, h * 0.18f)
            p.cubicTo(w * 0.92f, h * 0.28f, w * 0.88f, h * 0.58f, cx, h - 6.0f)
            p.close()
        }
        28 -> poly(12, 0.46f)
        29 -> {
            p.moveTo(6.0f, h * 0.62f)
            p.cubicTo(w * 0.20f, h * 0.30f, w * 0.30f, h * 0.90f, w * 0.50f, h * 0.56f)
            p.cubicTo(w * 0.70f, h * 0.22f, w * 0.80f, h * 0.82f, w - 6.0f, h * 0.46f)
            p.lineTo(w - 6.0f, h - 6.0f)
            p.lineTo(6.0f, h - 6.0f)
            p.close()
        }
        30 -> {
            val pointCount = 40
            val n = 2.0 + (s.variant % 6).toDouble() * 0.35
            val exponent = 2.0 / n
            for (i in 0..pointCount) {
                val t = i.toDouble() / pointCount.toDouble()
                val angle = t * 2.0 * Math.PI
                val cosine = cos(angle)
                val sine = sin(angle)
                val xPower = Math.pow(abs(cosine).toDouble(), exponent).toFloat()
                val yPower = Math.pow(abs(sine).toDouble(), exponent).toFloat()
                val xSign = if (cosine < 0.0) -1.0f else 1.0f
                val ySign = if (sine < 0.0) -1.0f else 1.0f
                val x = cx + rx * xSign * xPower
                val y = cy + ry * ySign * yPower
                if (i == 0) {
                    p.moveTo(x, y)
                } else {
                    p.lineTo(x, y)
                }
            }
            p.close()
        }
        else -> {
            p.moveTo(w * 0.30f, 6.0f)
            p.lineTo(w * 0.70f, 6.0f)
            p.lineTo(w * 0.70f, h * 0.30f)
            p.lineTo(w - 6.0f, h * 0.30f)
            p.lineTo(w - 6.0f, h * 0.70f)
            p.lineTo(w * 0.70f, h * 0.70f)
            p.lineTo(w * 0.70f, h - 6.0f)
            p.lineTo(w * 0.30f, h - 6.0f)
            p.lineTo(w * 0.30f, h * 0.70f)
            p.lineTo(6.0f, h * 0.70f)
            p.lineTo(6.0f, h * 0.30f)
            p.lineTo(w * 0.30f, h * 0.30f)
            p.close()
        }
    }
    return p
}

private fun readTheme(context:Context,uri:Uri):ThemeState{
    val entries=mutableMapOf<String,ByteArray>()
    ZipInputStream(context.contentResolver.openInputStream(uri)!!).use { z ->
        while(true){val e=z.nextEntry?:break;if(!e.isDirectory)entries[e.name]=z.readBytes()}
    }
    val txt=entries["theme.txt"]?.toString(Charsets.UTF_8)?:error("theme.txt غير موجود")
    var t=ThemeState()
    fun q(k:String)=Regex("""^$k\s*=\s*"([^"]*)"""",RegexOption.MULTILINE).find(txt)?.groupValues?.get(1)
    fun col(k:String)=Regex("""^$k\s*=\s*"(#[0-9A-Fa-f]{6,8})"""",RegexOption.MULTILINE).find(txt)?.groupValues?.get(1)?.let(::parseColor)
    t=t.copy(name=q("name")?:t.name,author=q("author")?:"",id=q("id")?:t.id,description=q("description")?:t.description,primary=col("primary")?:t.primary,background=col("background")?:t.background,background2=col("surface_container")?:t.background2)
    Regex("""^scale_text\s*=\s*([0-9.]+)""",RegexOption.MULTILINE).find(txt)?.groupValues?.get(1)?.toFloatOrNull()?.let{t=t.copy(textScale=it)}
    val font=q("font");val bg=q("image")
    if(font!=null)entries[font]?.let{t=t.copy(arabicFontName=font,arabicFontBytes=it)}
    if(bg!=null)t=t.copy(backgroundFileName=bg,backgroundImage=entries[bg])
    return t
}

private fun validateTheme(t:ThemeState){require(t.name.isNotBlank()){"اسم الثيم فارغ"};require(t.id.isNotBlank()){"ID الثيم فارغ"};require(t.groupStyles.isNotEmpty()){"لا توجد إعدادات أزرار"}}
private fun exportThemeToUri(context:Context,t:ThemeState,uri:Uri){val assets=buildAssets(t);val txt=buildThemeTxt(t,assets.keys.toList());ZipOutputStream(BufferedOutputStream(context.contentResolver.openOutputStream(uri)!!)).use{z->z.putNextEntry(ZipEntry("theme.txt"));z.write(txt.toByteArray(Charsets.UTF_8));z.closeEntry();assets.toSortedMap().forEach{(n,b)->z.putNextEntry(ZipEntry(n));z.write(b);z.closeEntry()}}}
private fun buildAssets(t:ThemeState):MutableMap<String,ByteArray>{val a=mutableMapOf<String,ByteArray>();KeyGroup.values().forEach{g->val s=t.groupStyles[g]?:GroupStyle();a["Shapes/group_${g.name.lowercase(Locale.US)}.png"]=renderShapePng(shapeLibrary.getOrElse(s.shapeId){shapeLibrary[0]},s.color1,s.color2,s.fillMode,t.borderColor,t.borderWidth)};t.keyOverrides.forEach{(k,s)->a["Shapes/key_${safe(k)}.png"]=renderShapePng(shapeLibrary.getOrElse(s.shapeId){shapeLibrary[0]},s.color1,s.color2,s.fillMode,t.borderColor,t.borderWidth)};t.keyImages.forEach{(k,b)->a["Key-${safe(k)}.png"]=b};t.backgroundImage?.let{a["background.png"]=it};val fontBytes=if(t.exportFontLanguage=="العربية")t.arabicFontBytes else t.englishFontBytes;val fontName=if(t.exportFontLanguage=="العربية")t.arabicFontName else t.englishFontName;fontBytes?.let{a[fontName?:"CustomFont.ttf"]=it};return a}
private fun buildThemeTxt(t:ThemeState,files:List<String>):String {
    fun c(x:Int)=String.format(Locale.US,"#%08X",x)
    fun n(x:Float)=String.format(Locale.US,"%.3f",x)
    val sb=StringBuilder()
    fun line(v:String=""){sb.append(v).append('\n')}
    line("# Generated by FUTO Theme Studio")
    line("# FUTO Keyboard Theme Configuration")
    line("# Format version: 1.0")
    line()
    line("name = \"${toml(t.name)}\"")
    line("author = \"${toml(t.author)}\"")
    line("id = \"${toml(t.id)}\"")
    line("version = 2")
    line("description = \"${toml(t.description)}\"")
    line()
    line("[options]")
    line("auto_borders = true")
    line("center_hints = false")
    line("roundedness = ${n((t.radius/60f).coerceIn(0f,1f))}")
    line("scale_text = ${n(t.textScale)}")
    line("scale_hints = 0.900")
    line("weight_text = 400")
    line("weight_hints = 400")
    line()
    val letter=t.groupStyles[KeyGroup.LETTERS]?:GroupStyle()
    line("[colors]")
    line("primary = \"${c(t.primary)}\"")
    line("on_primary = \"${c(t.onPrimary)}\"")
    line("background = \"${c(t.background)}\"")
    line("on_background = \"${c(letter.textColor)}\"")
    line("surface = \"${c(t.background)}\"")
    line("on_surface = \"${c(letter.textColor)}\"")
    line("surface_variant = \"${c(t.background2)}\"")
    line("on_surface_variant = \"${c(letter.textColor)}\"")
    line("keyboard_surface = \"${c(letter.color1)}\"")
    line("keyboard_surface_dim = \"${c(letter.color1)}\"")
    line("keyboard_container = \"${c(letter.color1)}\"")
    line("keyboard_container_variant = \"${c(letter.color2)}\"")
    line("on_keyboard_container = \"${c(letter.textColor)}\"")
    line("keyboard_press = \"${c(letter.pressed1)}\"")
    line("keyboard_container_pressed = \"${c(letter.pressed2)}\"")
    line("on_keyboard_container_pressed = \"${c(letter.textColor)}\"")
    line()
    val fontName=if(t.exportFontLanguage=="العربية")t.arabicFontName else t.englishFontName
    if(fontName!=null&&files.contains(fontName)){line("[options.font]");line("font = \"${toml(fontName)}\"");line()}
    if(files.contains("background.png")){line("[options.background]");line("image = \"background.png\"");line("opacity = 1");line("action_bar_opacity = 0.26");line("cropping = [0.0, 0.0, 1.0, 1.0]");line()}
    val order=listOf(KeyGroup.TOP,KeyGroup.LETTERS,KeyGroup.FUNCTIONAL,KeyGroup.BOTTOM,KeyGroup.SPECIAL,KeyGroup.SPACE,KeyGroup.ACTION)
    fun selectors(g:KeyGroup)=when(g){KeyGroup.TOP->listOf("functional row -1");KeyGroup.LETTERS->listOf("normal row 0","normal row 1","normal row 2");KeyGroup.FUNCTIONAL->listOf("functional");KeyGroup.BOTTOM->listOf("normal row -1");KeyGroup.SPECIAL->listOf("normal popup");KeyGroup.SPACE->listOf("spacebar");KeyGroup.ACTION->listOf("action")}
    t.keyImages.filterKeys{!it.startsWith("GROUP_")}.forEach{(k,_)->val asset="Key-${safe(k)}.png";if(files.contains(asset)){line("[[matchrules.border]]");line("selector = \"normal label ${escapeSelector(k)}\"");line("asset = \"$asset\"");line()}}
    t.keyOverrides.forEach{(k,_)->val asset="Shapes/key_${safe(k)}.png";line("[[matchrules.border]]");line("selector = \"normal label ${escapeSelector(k)}\"");line("asset = \"$asset\"");line()}
    order.forEach { g ->
        val s=t.groupStyles[g]?:GroupStyle()
        s.imageKey?.let { key ->
            val asset="Key-${safe(key)}.png"
            if(files.contains(asset)) selectors(g).forEach { sel ->
                line("[[matchrules.border]]")
                line("selector = \"$sel\"")
                line("asset = \"$asset\"")
                line()
            }
        }
    }
    order.forEach { g ->
        val asset="Shapes/group_${g.name.lowercase(Locale.US)}.png"
        selectors(g).forEach { sel ->
            line("[[matchrules.border]]")
            line("selector = \"$sel\"")
            line("asset = \"$asset\"")
            line()
        }
    }
    files.filter{it.startsWith("Shapes/")||it.startsWith("Key-")}.forEach { n ->
        line("[[asset.border]]")
        line("name = \"${toml(n)}\"")
        line("background_tint = \"#FFFFFFFF\"")
        line("foreground_tint = \"#FFFFFFFF\"")
        line("padding = [0, 0, 0, 0]")
        line("slicing = [0.300, 0.300, 0.700, 0.700]")
        line("gap = [1, 1, 1, 1]")
        line("target_density = 640")
        line()
    }
    return sb.toString()
}

private fun renderShapePng(spec:ShapeSpec,c1:Int,c2:Int,mode:FillMode,border:Int,borderWidth:Float):ByteArray{val size=256;val bmp=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);val canvas=Canvas(bmp);val path=pathForShape(spec,size.toFloat(),size.toFloat());val p=Paint(Paint.ANTI_ALIAS_FLAG);if(spec.threeD){p.shader=LinearGradient(spec.depth,spec.depth,size.toFloat(),size.toFloat(),android.graphics.Color.BLACK.withAlpha(120),c1,Shader.TileMode.CLAMP);val ex=pathForShape(spec,size.toFloat(),size.toFloat());ex.offset(spec.depth,spec.depth);canvas.drawPath(ex,p)};p.shader=if(mode==FillMode.GRADIENT)LinearGradient(0f,0f,size.toFloat(),size.toFloat(),c1,c2,Shader.TileMode.CLAMP)else null;p.color=c1;p.alpha=255;canvas.drawPath(path,p);if(spec.threeD){p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=4f;p.color=android.graphics.Color.WHITE.withAlpha(90);canvas.drawPath(path,p)};if(borderWidth>0){p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=borderWidth*2;p.color=border;canvas.drawPath(path,p)};return ByteArrayOutputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it);it.toByteArray()}}
private fun Int.withAlpha(a:Int)=android.graphics.Color.argb(a,android.graphics.Color.red(this),android.graphics.Color.green(this),android.graphics.Color.blue(this))
private fun parseColor(s:String):Int?=runCatching{val x=s.removePrefix("#");when(x.length){6->("FF$x").toLong(16).toInt();8->x.toLong(16).toInt();else->null}}.getOrNull()
private fun lighten(c:Int):Int{val r=android.graphics.Color.red(c);val g=android.graphics.Color.green(c);val b=android.graphics.Color.blue(c);return android.graphics.Color.argb(255,(r+(255-r)*.35).roundToInt(),(g+(255-g)*.35).roundToInt(),(b+(255-b)*.35).roundToInt())}
private fun safe(s:String)=s.map{if(it in 'A'..'Z'||it in 'a'..'z'||it in '0'..'9'||it=='_'||it=='-')it else '_'}.joinToString("").ifBlank{"key"}
private fun safeFile(s:String)=safe(s).take(48).ifBlank{"futo-theme"}
private fun toml(s:String)=s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ")
private fun escapeSelector(s:String)=s.replace("\\","\\\\").replace("\"","\\\"")
