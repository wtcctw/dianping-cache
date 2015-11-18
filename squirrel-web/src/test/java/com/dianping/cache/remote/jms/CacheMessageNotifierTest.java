package com.dianping.cache.remote.jms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;

import edu.emory.mathcs.backport.java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/applicationContext-zookeeper-notify-test.xml")
public class CacheMessageNotifierTest {

    private static Logger logger = LoggerFactory.getLogger(CacheMessageNotifierTest.class);
    
    private String[] CATEGORIES = { "TGDealGroupMain", "TGDealGroupMainDetail", "TGDealGroupBase", "rs-web",
            "oStaticFileMD5", "CortexDependency", "CortexCombo", "DianPing.Common.StaticFile", "oHeaderTemplate",
            "TGMovieDiscount", "FSDealGroupListCache", "TGMovieActivity", "FSCityDealIdListCache",
            "TG_CinemaIdsWithMovieShow", "FSNowDealIdListCache", "TG_Movie", "TG_MovieSort", "FSDealRealTimeCache",
            "FSDealRealTimeListCache", "TGMovieDiscountNotEndedItemId", "TGMovieDiscountActivityRule", "oUrlRegexList",
            "TGMovieShowBlackList", "TGMinMovieShowPriceByCinema", "TGMovieDiscountVoucherAvailableCount",
            "DianPing.Common.ConfigurationDAC", "FSWannaJoinListCache", "TG_MovieDynamic", "FSWannaJoinCache",
            "DianPing.Common.CityDAC", "TGApiConfig", "oConfig", "PCTDiscountGroup", "TGPopularizationEvent",
            "TGMovieDiscountActivity", "TGThirdPartyCinemaMap", "TGMovieActivityInfo", "TG_Cinema", "TGMovieDetail",
            "TGMovieEvent", "TG_CinemaIdsWithDealGroup", "EsOsEmployee", "oSMSTypeInfo", "oCityBizConfig",
            "TGCinemaSeatList", "TGEventTopic", "TGMovieCinemaListOfRegion", "TGNaviTagBO", "TGNaviTagConfigAll",
            "mApi-java-web", "TGNaviCategory", "TGNaviCategoryTreeServer", "TGIndexAdvertiseImage",
            "TGMovieShowThirdPartyInfoMap", "TGMovieIndexDealGroupList", "oShopListCityShopSpecRankingControls",
            "TGNaviTagByEnNameServer", "TGEventTopicByCityV1", "TGNaviCategoryCity", "TGESecondPrizeById",
            "TGEventTopicDTO", "TGMovieBoxOfficeRank", "oAdItem_Template_Map", "mobile-web", "lBadgeDefinition",
            "TGESecondPrizeByBeginDate", "lBadgeIds", "oShopListCityShopRankingControls", "oActiveCityToneShopIDList",
            "TGNaviCategoryTree", "DSDealGroupTextAttr", "TGMovieEventCityIds", "toEventPromoReduceList", "oMailList",
            "TGRetinaImageDetailDTO", "TGMessageTipByCityAndPosition", "TGNaviSearchByCategoryIdAndTagItemId",
            "lTGNationwideDealGroupIds", "TGMovieStatistics", "TG_MovieStatistics", "TGMovieNews",
            "TGNaviCategoryActive", "TGWebEvents", "oIndexAlpabetCities", "TG_AllCinemaIds", "TGNaviTagItem",
            "TGNaviTagPriceByCity", "TGRegionCities", "mobile-api-ums", "TGThirdPartyCinemaID", "TGNaviTagByIdServer",
            "TGDealGroupExtListByCityID", "TGCityHomeDistricts", "TGTagForSearch", "TGMovieCast", "TGHotKeyWord",
            "shop-leo", "oNavUrlList", "TGNaviTagItemList", "TGNaviSearchCache" };

    @Autowired
    private CacheMessageNotifier cacheMessageNotifier;
    private Random random = new Random();

    @Test
    public void testNotifyKeyRemove() throws Exception {
        int categories = 10;
        int messagesPerCategory = 500;
        int totalMessages = categories * messagesPerCategory;
        long start = System.currentTimeMillis();
        List<SingleCacheRemoveDTO> messages = generateCacheRemoveMessages(categories, messagesPerCategory);
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for(final SingleCacheRemoveDTO message : messages) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    cacheMessageNotifier.sendMessageToTopic(message);
                }
            });
        }
        long span = System.currentTimeMillis() - start;
        logger.info("sent " + totalMessages + " in " + span + "ms");
        System.in.read();
    }

    private List<SingleCacheRemoveDTO> generateCacheRemoveMessages(int categoryCount, int messagesPerCategory) {
        List<SingleCacheRemoveDTO> messages = new ArrayList<SingleCacheRemoveDTO>(categoryCount * messagesPerCategory);
        for(int i=0; i<categoryCount; i++) {
            messages.addAll(generateCacheRemoveMessages(CATEGORIES[i], messagesPerCategory));
        }
        Collections.shuffle(messages);
        return messages;
    }
    
    private List<SingleCacheRemoveDTO> generateCacheRemoveMessages(String category, int count) {
        List<SingleCacheRemoveDTO> messages = new ArrayList<SingleCacheRemoveDTO>(count);
        for(int i=0; i<count; i++) {
            messages.add(generateCacheRemoveMessage(category));
        }
        return messages;
    }

    private SingleCacheRemoveDTO generateCacheRemoveMessage() {
        return generateCacheRemoveMessage(CATEGORIES[random.nextInt(CATEGORIES.length)]);
    }
    
    private SingleCacheRemoveDTO generateCacheRemoveMessage(String category) {
        SingleCacheRemoveDTO message = new SingleCacheRemoveDTO();
        message.setCacheType("web");
        message.setCacheKey(category + "." + RandomStringUtils.randomAlphabetic(8) + "_10");
        return message;
    }

}
