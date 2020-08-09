package edu.rosehulman.fangr.kitchenkit

object Utils {
    val ingredientNameToUrl: Map<String, String> =
        mapOf("apple cider vinegar" to "http://health-zoom.worldwideshoppingmall.co.uk/TOL62453.jpg",
            "bacon" to "http://www.owaves.com.sg/image/cache/data/Pork/Steacky%20Bacon%20Ps-500x500.png",
            "beef" to "http://weknowyourdreams.com/images/beef/beef-09.jpg",
            "black pepper" to "https://i.ebayimg.com/00/s/NTYyWDU2Mg==/z/TdYAAOSwDNdVyMiT/${'$'}_35.JPG",
            "butter" to "https://sc01.alicdn.com/kf/UTB8mvkDX1vJXKJkSajhq6A7aFXaz/Unsalted-Butter-82-.jpg_350x350.jpg",
            "buttermilk" to "https://d3cizcpymoenau.cloudfront.net/images/20636/SIL_Buttermilk_Garelick480.jpg",
            "broccoli" to "http://karendevine.co.uk/wp-content/uploads/2015/04/Broccoli1.jpg",
            "brown sugar" to "https://cdn.shopify.com/s/files/1/0267/1841/products/7043-brown-sugar-flavor_1200x.jpeg?",
            "chicken tender" to "https://foodmateus.com/wp-content/uploads/2018/08/chicken_tenderloins_resized_large_1.jpeg",
            "dried cranberry" to "https://www.duncraft.com/common/images/products/large/3000R_zoom.jpg",
            "flour" to "https://shop.kingarthurbaking.com//item-img/3020_03_29_2016__11_15_32_700",
            "fresh sage" to "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2013/8/9/1/FN_sage-thinkstock_s4x3.jpg.rend.hgtvcom.616.462.suffix/1382545713166.jpeg",
            "garlic" to "http://www.italianfoodforever.com/wp-content/uploads/2017/03/garlic.jpg",
            "garlic powder" to "https://target.scene7.com/is/image/Target/GUEST_29ba29c4-1d98-4ecc-9f93-c9bac35df386?wid=488&hei=488&fmt=pjpeg",
            "honey" to "https://5.imimg.com/data5/UQ/LH/MY-66919833/glass-honey-jar-250-ml-2-500x500.jpg",
            "mayonnaise" to "https://target.scene7.com/is/image/Target/13028016",
            "orange juice" to "https://media.fooducate.com/products/images/180x180/009143C5-56A2-9A47-EDBA-87021B9B3D33.jpg",
            "peanut oil" to "https://i5.walmartimages.com/asr/19bccfca-9bbd-4b4d-a022-74c00b7d2ddc_1.6f54fb113b7e654809b2fc2a3ea40099.jpeg?odnHeight=450&odnWidth=450&odnBg=FFFFFF",
            "potato" to "https://images.vice.com/noisey/content-images/contentimage/96940/potato-013.jpg",
            "red onion" to "https://www.markon.com/sites/default/files/styles/large/public/pi_photos/Onions_Red_Hero.jpg",
            "salt" to "https://www.ldoceonline.com/media/english/illustration/salt.jpg",
            "sweet paprika" to "https://i5.walmartimages.com/asr/f29169bc-aa00-4735-bb41-2c1135e19e0e_1.d71970c7d7670482f98628917648e906.jpeg?odnHeight=450&odnWidth=450&odnBg=ffffff",
            "turkey" to "https://images.huffingtonpost.com/2008-11-25-rawturkey_300.jpg",
            "vegetable oil" to "https://i5.walmartimages.com/asr/eb8ce838-eace-46be-8cf3-8fc7b7f566c2_1.35ceca091cf5dc563d11230152dc1d60.jpeg?odnHeight=450&odnWidth=450&odnBg=ffffff"
            )

    fun getIngUrlFromName(name: String): String? {
        return ingredientNameToUrl[name]
    }
}