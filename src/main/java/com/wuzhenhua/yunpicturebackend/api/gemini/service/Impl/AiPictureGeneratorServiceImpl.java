package com.wuzhenhua.yunpicturebackend.api.gemini.service.Impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import cn.hutool.core.util.ObjUtil;
import com.google.genai.types.*;
import com.wuzhenhua.yunpicturebackend.api.gemini.Gemini;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.AiGenerateResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateChatRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.CreateImageRequest;
import com.wuzhenhua.yunpicturebackend.api.gemini.model.ImageResponse;
import com.wuzhenhua.yunpicturebackend.api.gemini.service.AiPictureGeneratorService;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.entity.User;

import com.wuzhenhua.yunpicturebackend.model.enums.UserRoleEnum;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryService;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.service.impl.AiChatSpaceBindingUtils;

import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
public class AiPictureGeneratorServiceImpl implements AiPictureGeneratorService {

    @Resource
    Gemini gemini;
    @Resource
    UserService userService;
    @Resource
    PictureService pictureService;
    @Resource
    ChatHistoryService chatHistoryService;

    @Override
    @Deprecated
    public ImageResponse generateImages(CreateImageRequest request, HttpServletRequest httpServletRequest) {
        // 1.婵☆偀鍋撳Δ鐘茬灱閺併倝骞?
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "User must be logged in");
        String prompt = request.getPrompt();
        log.info("Prompt: {}", prompt);
        // 闁告帇鍊栭弻鍥炊閸撗冾暬
        MultipartFile file = request.getFile();
        ImageResponse imageResponse = new ImageResponse();

        // 闂佹澘绉堕悿?Gemini 闁汇垻鍠愰崹姘跺礃閸涱収鍟?
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .build();

        try {
            List<Content> contents;
            if (file == null) {
                // 濞寸姴鎳忛弸鍐偓娑欘殘閺佹捇骞嬮幇顒佺闁?
                log.info("闁告瑦鍨块埀顑胯兌閸戜粙寮崶銊︽嫳閻犲洭鏀遍惇浼村礆?Gemini API, prompt length: {}", prompt.length());
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(prompt)))
                        .build());
            } else {
                // 闁搞儱澧芥晶?闁哄倸娲ら悺褔鎮介悢绋跨亣闁哄倹婢樺ù姗€鎮?
                // 1. 闁稿繐鐗呯粭鍌涘閻樻彃鐓?COS闁挎稑鐭侀獮蹇涘矗閺嵮冪缂傚倵鏅涢幃妤呮儍閸曨偅绂堥柣?
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                PictureVO uploadPicture = pictureService.uploadPicture(
                        file, pictureUploadRequest, loginUser);
                imageResponse.setUploadPicture(uploadPicture);

                // 2. 濞村吋锚閸樻稒鎷呯捄銊︽殢 thumbnailUrl闁挎稑鑻々褔寮稿鍕⒕闁哄牆顦崹顖炴⒔瀹ュ洭鐛撳ù锝堟硶閺?url
                String imageUrl = uploadPicture.getThumbnailUrl();
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = uploadPicture.getUrl();
                    log.warn("闁搞儱澧芥晶?{} 婵炲备鍓濆﹢浣虹磽閳哄啯娈ｉ柛銉︽嫕缁辨繃鎷呯捄銊︽殢闁告鍠庡ù?URL", uploadPicture.getId());
                }
                byte[] compressedImageBytes = downloadImageFromUrl(imageUrl);

                log.info("闁告瑦鍨块埀顑跨濞存﹢鎮?闁哄倸娲﹀﹢鎵嫚闁垮婀撮柛?Gemini API, 闁告鍠庨～鎰緞瑜嶉惃? {} bytes, 闁告ê顑囩紓澶愬触? {} bytes, prompt length: {}",
                        file.getSize(), compressedImageBytes.length, prompt.length());

                // 3. 闁哄瀚紓鎾诲礌閸涱厽鍎撻柛銉ュ⒔婢ф牠宕仦鐐€悗娑欘殘濞堟垹鎷犻柨瀣勾闁告劕鎳庨?
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(
                                Part.fromText(prompt),
                                Part.fromBytes(compressedImageBytes, "image/webp")))
                        .build());
            }

            // 閻犲鍟伴弫?Gemini API闁挎稑鐗呮繛鍥偨閵娾晜濮滄繛缈犵缁憋繝骞掗妷銉ョ稉闁挎稑鐭傛导鈺呭礂?SDK 婵炵繝绀佺槐锛勬喆閿濆棛鈧?bug闁?
            GenerateContentResponse response = gemini.client.models.generateContent(gemini.modelName, contents, config);

            // 濠㈣泛瀚幃?Gemini 閺夆晜鏌ㄥú鏍儍閸曨偅鎯欓幖?
            if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                List<Part> parts = response.candidates().get().get(0).content().get().parts().get();
                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        // 濠㈣泛瀚幃濠囧炊閸撗冾暬闁轰胶澧楀畵?
                        Blob inlineData = part.inlineData().get();
                        if (inlineData.data().isPresent()) {
                            byte[] generatedImageBytes = inlineData.data().get();
                            String generatedMimeType = inlineData.mimeType().orElse("image/png");
                            String fileName = "ai_generated_" + UUID.randomUUID()
                                    + getExtensionFromMimeType(generatedMimeType);

                            log.info("闁衡偓鐠哄搫鐓?Gemini 闁汇垻鍠愰崹姘舵儍閸曨偅绂堥柣? size: {} bytes, mimeType: {}",
                                    generatedImageBytes.length, generatedMimeType);

                            // 闁告帗绋戠紓?MultipartFile 妤犵偞婀圭粭鍌涘閻樻彃鐓?COS
                            MultipartFile generatedFile = new Base64DecodedMultipartFile(
                                    generatedImageBytes, fileName, generatedMimeType);

                            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                            PictureVO generatePicture = pictureService.uploadPicture(
                                    generatedFile, pictureUploadRequest, loginUser);
                            imageResponse.setGeneratePicture(generatePicture);
                        }
                    } else if (part.text().isPresent()) {
                        // 濠㈣泛瀚幃濠囧棘閸ャ劍鎷遍柡浣哄瀹?
                        String text = part.text().get();
                        log.info("闁衡偓鐠哄搫鐓?Gemini 闁哄倸娲﹀﹢浼村传瀹ュ懐瀹? {}", text);
                        imageResponse.setText(text);
                    }
                }
            }

            log.info("Gemini API response handled successfully");
            return imageResponse;

        } catch (IOException e) {
            log.error("Image processing failed", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Image processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Gemini API 閻犲鍟伴弫銈嗗緞鏉堫偉袝", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini API 閻犲鍟伴弫銈嗗緞鏉堫偉袝:" + e.getMessage());
        }
    }

    @Override
    public AiGenerateResponse generateAiImage(CreateChatRequest request, HttpServletRequest httpServletRequest) {
        // 1. 濡ょ姴鐭侀惁澶愭偨閵婏箑鐓?
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "User must be logged in");
        String prompt = request.getPrompt();
        List<Long> pictureIds = request.getPictureIds();
        Long requestSpaceId = request.getSpaceId();
        Long existingSessionSpaceId = getExistingSessionSpaceId(request.getSessionId(), loginUser.getId());
        Long effectiveChatSpaceId = AiChatSpaceBindingUtils.resolveChatSpaceId(
                requestSpaceId, existingSessionSpaceId, request.getSessionId() != null);

        // 2. 闁哄稄绻濋悰娆撳炊閸撗冾暬闁轰椒鍗抽崳娲晬閸喐浠樺?4鐎殿喚濯寸槐?
        if (pictureIds != null && pictureIds.size() > 14) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Too many reference pictures");
        }
        ChatHistory userChatHistory = null;

        if (effectiveChatSpaceId == null || ObjUtil.isNull(effectiveChatSpaceId)) {
            // 3. 濞ｅ洦绻傞悺銊╂偨閵婏箑鐓曟繛鎴濈墛娴煎懘宕?chat_history闁挎稑鐗婇弫顕€骞愭担鍓叉▼闁搞儱澧芥晶鏍晬?
            userChatHistory = chatHistoryService.saveUserMessage(
                    loginUser.getId(), prompt, pictureIds, request.getSessionId());
            log.info("generateAiImage - 闁活潿鍔嶉崺娑樷槈閸喍绱栫€规瓕寮撶换姘扁偓? chatHistoryId: {}", userChatHistory.getId());
        } else {
            // 3. 濞ｅ洦绻傞悺銊╂偨閵婏箑鐓曟繛鎴濈墛娴煎懘宕?chat_history闁挎稑鐗婇弫顕€骞愭担鍓叉▼闁搞儱澧芥晶鏍晬?
            userChatHistory = chatHistoryService.saveUserMessage(
                    loginUser.getId(), prompt, pictureIds, request.getSessionId(), effectiveChatSpaceId);
            log.info("generateAiImage - 闁活潿鍔嶉崺娑樷槈閸喍绱栫€规瓕寮撶换姘扁偓? chatHistoryId: {}", userChatHistory.getId());
        }

        AiGenerateResponse response = new AiGenerateResponse();

        // 闂佹澘绉堕悿?Gemini 闁汇垻鍠愰崹姘跺礃閸涱収鍟?
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .systemInstruction(
                        Content.fromParts(Part.fromText("""
                                # 閻熸瑦甯熸竟濠冪▔鎼达絾绐楅柡?
                                
                                濞达絿濮靛Σ鍛婄▔閳ь剚鎷呭澶屽蒋缂?AI 闁煎湱鍎ゅ﹢鎶藉箑閼姐倖纾ч柛婊冭嫰濞存﹢宕撹箛鏇熸櫢闁瑰瓨鍔掔粭鎾垛偓纭呯堪閳ь剙鍊风紞姗€鎯冮崟顓熺獥闁哄秴娲﹀Σ鎼佸春鏉炴壆鑹鹃柣顫妽閸╂盯鎯冮崟鈹惧亾閹邦厽鐎悗娑欘殕瀵寧娼婚懜顑藉亾閹存繃瀚查梺鏉跨Ф閻ゅ棝鎯冮崟鈹惧亾閹邦剙妫橀柤鏉垮暙濞存﹢鎮ч崶銉㈠亾閹搭垳绀夐柛姘墛閸ㄦ碍绋夐埀顒€顕ｉ悩鍨毎闂傚牄鍨荤划鐑樼▔閳ь剟濡存笟鈧悵顔炬嫻閵娾晛娅ら柣銊ュ濞存﹢宕撹箛瀣у亾?
                                
                                # 閺夊牊鎸搁崣鍡涘极閻楀牆绁?
                                
                                - **闁活潿鍔嶉崺娑㈠箵韫囨艾鐗?*: 
                                - **闁告瑥鍊介埀顒€鍟ù姗€鎮?*: 闂傚嫬瀚▎銏＄▔椤撶姵鐣遍柛銉ュ⒔婢ф牠鏁嶉崼鐔镐粯濠?4鐎殿喚濯寸槐姘跺及椤栨艾褰犻梺娆惧枤濞堟垹鎲撮崱姘兼綍濡炲瀛╅悧鎼佸箰閸パ冪闁?
                                
                                # 闁圭瑳鍡╂斀闁圭娲ｉ幎?
                                
                                1. **婵烇絽宕€规娊宕氶崱妯尖偓浠嬪矗閸屾績鍋撻崘銊︾**: 濞寸姵姊荤划蹇涘礆閸℃鈧粙姊介崟顏咁偨濞戞搩鍘惧▓鎴﹀矗閸屾績鍋撻崘銊︾闁绘娴勭槐婵嬪箵閹邦剙绲垮ù鐘劙缁楀懐鎲版担铏诡槺闁?
                                    - *閻熸瑥妫滈～搴㈩槹鎼淬垻澹?: 闁煎湱鍎ゅ﹢铏垔閹哄秶鐭欓柨娑樼墕椤┭冣柦閸︻厽鏆伴柕?D婵炴挸寮堕悡瀣Υ娴ｈ鍟旂憸鐗堝敾缁辨岸濡存担鍝勬疇闁哄鈧剚妲遍柣鐐叉閹蜂即姊奸弶鎴濐殯濡炲瀛╅悧鎼佸Υ?
                                    - *闁艰褰冮崓鐢稿级?: 濞戞挻妲掓竟濠勬嫬閸愶腹鍋撴担绋垮辅闁绘挆鍡楊棌婵炴挴鏅涢幏鐗堫殰閸楃偞瀚查幖杈捐礋閳?
                                    - *婵ɑ绋戝ú?: 闁轰胶绻濈紞瀣箚閸涱垰宕曢柨娑樼墕椤┭呯矚閾忛€涚触闁靛棔娴囩粋宀勫础濮橆厽绠欓柛蹇擃儍閳ь兛鐒﹂悗顒傜不閳ь剚绋夌拋宕囩枀闁挎稑顦埀?
                                2. **闁告艾鐗婇崹姘舵偨閻旂鐏?*: 閻忓繐妫庨埀顒佸姉閺併倝骞嬮柨瀣紟閺夆晞鍩囬埀顒佸灣閼垫垹鈧鐭粻鐔兼儍閸曨偄寰斿ù?*濞戞捁顔婄紞瀣礃閸涱収鍟?*闁挎稑濂旂粭灞剧鎼存稈鍋撻幇顒€妫橀柤鏉垮暙濞存﹢鎮ч崶銉㈠亾閹存粏鍘柟缁樺姇瑜板洭鎯?*闁哄秴鍢茬槐锟犲椽鐏炲墽姣ら柛?*闁烩晛鎽滅划銊╁触閸稈鍋?
                                    - *濞村吋锚閸樻稓鐥椤宕?: 闁靛棙鍔楅弫銈夊箣闁垮浼庨弶鈺勫焽閳ь剚鍨甸崰鍛偓瑙勫焹閳ь剚绮庨弫鐐閳ь剚绋婇崼姘ｅ亾濠垫挾绀勯柛鎰噹椤旀劙鏁嶆径娑氱闁靛棙鍔曞顒勬嚀閸愩劍绂堥柣妤€娲㈤埀顒佸灥閸犲懐鈧鍩冮埀顒佺矋閳ь剙绨肩粻鐐烘偨閻戔斁鍋撳┑鎾剁鐟滆埇鍨圭槐?濡炲瀛╅悧鎼佹晬婢跺牃鍋?
                                3. **闁瑰灈鍋撻柡鍫灥椤鎳?*:
                                    - 缁绢収鍠曠换姘殗濡湱绠介柣顏嗗枎鐎规娊鏁嶇仦鍏肩溄濞达絾鎹佽闁告挻鐗楅惁顔界瑹鐎ｎ偒鍔€缁绢収鍣槐婵嬪礂婢跺苯寮鹃梺顐ｆ缁额偅绋夐埀顒勬嚊濞ｎ兘鍋?
                                    - 濠碘€冲€归悘澶愬矗閸屾績鍋撻崘銊︾闁绘娲ˉ鎾诲冀閼测晜绁插ù婊勫笒閸熻法绮ｆ笟濠勭閻犲洩娓圭槐顓㈠礂閸繂妫橀柤鏉垮暙婢?3 鐎殿喚濮村ù姗€鎮ч崶鈺傜暠濡炲瀛╅悧鎼佸Υ?
                                    - 閺夊牊鎸搁崵顓㈠礆閸℃岸鍝洪柣婊冩祫缁辩増顨囧鍫㈢缁绢収鍠曠换姘辩磼閸℃艾螡閻庨潧妫楃€瑰磭绮敃鈧幃搴㈢▔閹捐尙鐟归柡宥呮搐閸ｎ垶鏁嶆径鍫氬亾?
                                
                                # 闁汇垻鍠愰崹姘跺川閹存帗濮?
                                
                                缂佹柨顑呭畵鍡涙偨閻旂鐏囬柛銉﹀劤閸庢岸鏁嶇仦鐓庣船闁哄秴銈告导鎺戭嚗椤忓嫬妫橀柤鏉垮暙濞存﹢鎮ч崶鈺傜暠閻熸瑥妫滈～搴ｆ嫚椤撯檧鏋呴柨娑樿嫰閼荤喐鎯旈弮鍌涙殢濞存粌绨兼禍鎺撶▔鐎ｎ偄浼庨弶鈺傚濞堟垿宕烽悜妯荤彲闁?
                                闁靛棙鍔楅弫銈夊箣闁垮浼庨弶鈺勫焽閳? %s
                                
                                --
                                *濞ｅ洦绻冪€垫棃鎳涢鍡楀Ё闁汇劌瀚崢婊堟偂瑜嬮埀顑胯兌椤戜線宕ラ崼銏犫挅闁荤偛妫滈～澶婎嚗鐎ｎ剚鐣遍梻鍐╂綑婵傛牠鏁嶇仦闂寸鞍闁告瑥锕ゅ顒勬嚀閸愩劍绂堥柣妤€娲ｉ懙鎴犳兜椤旂厧鐎奸柣銊ュ濮规鎮堕崱姘獩闁规壆鍠嗛埀?
                                """)))
                .build();

        try {
            List<Content> contents;

            if (pictureIds == null || pictureIds.isEmpty()) {
                // 濞寸姴鎳忛弸鍐偓娑欘殘閺佹捇骞嬮幇顒佺闁?
                log.info("generateAiImage - 闁告瑦鍨块埀顑胯兌閸戜粙寮崶銊︽嫳閻犲洭鏀遍惇浼村礆?Gemini API");
                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(prompt)))
                        .build());
            } else {
                // 濠㈣埖鑹惧ù姗€鎮?闁哄倸娲ら悺褔鎮介悢绋跨亣闁哄倹婢樺ù姗€鎮?
                List<Part> partsList = new ArrayList<>();
                partsList.add(Part.fromText(prompt));

                // 濞戞挸顑堝ù鍥嵁閼搁潧娼戦柛鏃傚У婢у秹寮垫径濠冪闁?
                for (Long pictureId : pictureIds) {
                    // 缂佸苯鎼埀顒€鍚嬮ˉ鍛村蓟閵夘垳绐楃痪顓у枙缁绘岸宕堕崜褍顣婚悗娑櫭﹢?
                    var pictureEntity = pictureService.getById(pictureId);
                    if (pictureEntity == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                                "闁搞儱澧芥晶鏍ㄧ▔瀹ュ懐鎽犻柛? pictureId: " + pictureId);
                    }

                    Long pictureSpaceId = pictureEntity.getSpaceId();

                    // 濠碘€冲€归悘澶愬炊閸撗冾暬閻忕偟鍋樼花顒勫蓟閹邦亪鍤嬬紒灞炬そ濡潡鏁嶅畝鍐閻炴稑鏈鍫ユ⒔閹邦収姊鹃柡?
                    if (ObjUtil.isNotNull(pictureSpaceId)) {
                        // 闂傚牏鍋熼鎼佹偠閸℃鍠呴柣顫妽閸╂盯宕ｉ鍥у幋濞达綀娉曢弫銈夋嚊椤忓嫮绠掗柣銊ュ閳规牠姊婚弶鎴炵闁?
                        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
                            ThrowUtils.throwIf(!loginUser.getId().equals(pictureEntity.getUserId()),
                                    ErrorCode.NO_AUTH_ERROR, "Cannot use pictures from other users spaces");
                        }
                        // 濠碘€冲€归悘澶嬬鐠佸磭顏遍柛銉ュ⒔婢ф牠寮?spaceId闁挎稑鐬奸弫鎾诲箣閹邦喗鐣遍柛銉ュ⒔婢ф牗绋婇悢鍛婃澒闁告帗濯介姘辩矚濞差亝锛?
                    }

                    PictureVO picture = pictureService.getPictureVO(pictureEntity, httpServletRequest);
                    if (picture == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                                "闁哄啰濮电涵鍫曟嚔瀹勬澘绲块柛銉ュ⒔婢ф牗绌遍埄鍐х礀, pictureId: " + pictureId);
                    }
                    // 濞村吋锚閸樻稒鎷呯捄銊︽殢 thumbnailUrl闁挎稑鑻々褔寮稿鍕⒕闁哄牆顦崹顖炴⒔瀹ュ洭鐛撳ù锝堟硶閺?url
                    String imageUrl = picture.getThumbnailUrl();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = picture.getUrl();
                        log.warn("闁搞儱澧芥晶?{} 婵炲备鍓濆﹢浣虹磽閳哄啯娈ｉ柛銉︽嫕缁辨繃鎷呯捄銊︽殢闁告鍠庡ù?URL", pictureId);
                    }
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR,
                                "闁搞儱澧芥晶?URL 濞戞捁娅ｉ埞? pictureId: " + pictureId);
                    }
                    byte[] imageBytes = downloadImageFromUrl(imageUrl);
                    partsList.add(Part.fromBytes(imageBytes, "image/webp"));
                }

                log.info("generateAiImage - 闁告瑦鍨块埀顑跨盀}鐎殿喚濮村ù姗€鎮?闁哄倸娲﹀﹢鎵嫚闁垮婀撮柛?Gemini API", pictureIds.size());

                contents = ImmutableList.of(Content.builder()
                        .role("user")
                        .parts(ImmutableList.copyOf(partsList))
                        .build());
            }

            // 閻犲鍟伴弫?Gemini API
            GenerateContentResponse geminiResponse = gemini.client.models.generateContent(
                    gemini.modelName, contents, config);

            String aiText = null;
            List<PictureVO> generatedPictures = new ArrayList<>();

            // 濠㈣泛瀚幃?Gemini 閺夆晜鏌ㄥú鏍儍閸曨偅鎯欓幖?
            if (geminiResponse.candidates().isPresent() && !geminiResponse.candidates().get().isEmpty()) {
                List<Part> parts = geminiResponse.candidates().get().get(0).content().get().parts().get();
                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        Blob inlineData = part.inlineData().get();
                        if (inlineData.data().isPresent()) {
                            byte[] generatedImageBytes = inlineData.data().get();
                            String generatedMimeType = inlineData.mimeType().orElse("image/png");
                            String fileName = "ai_generated_" + UUID.randomUUID()
                                    + getExtensionFromMimeType(generatedMimeType);

                            log.info("generateAiImage - 闁衡偓鐠哄搫鐓?Gemini 闁汇垻鍠愰崹姘舵儍閸曨偅绂堥柣? size: {} bytes",
                                    generatedImageBytes.length);

                            MultipartFile generatedFile = new Base64DecodedMultipartFile(
                                    generatedImageBytes, fileName, generatedMimeType);

                            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                            if (effectiveChatSpaceId != null) {
                                pictureUploadRequest.setSpaceId(effectiveChatSpaceId);
                            }
                            PictureVO generatedPicture = pictureService.uploadPicture(
                                    generatedFile, pictureUploadRequest, loginUser);
                            generatedPictures.add(generatedPicture);
                        }
                    } else if (part.text().isPresent()) {
                        aiText = part.text().get();
                        log.info("generateAiImage - 闁衡偓鐠哄搫鐓?Gemini 闁哄倸娲﹀﹢浼村传瀹ュ懐瀹? {}", aiText);
                    }
                }
            }

            // 濞ｅ洦绻傞悺?AI 婵炴垵鐗婃导鍛村礆?chat_history闁挎稑鐗婇弫顕€骞愭担鍓叉▼闁搞儱澧芥晶鏍晬?
            List<Long> generatedPictureIds = generatedPictures.stream()
                    .map(PictureVO::getId)
                    .toList();
            Long userSessionId = userChatHistory.getSessionId();

            ChatHistory aiChatHistory = null;
            if (effectiveChatSpaceId == null || ObjUtil.isNull(effectiveChatSpaceId)) {
                aiChatHistory = chatHistoryService.saveAiMessage(
                        loginUser.getId(), aiText != null ? aiText : "", generatedPictureIds, userSessionId);
            } else
                aiChatHistory = chatHistoryService.saveAiMessage(
                        loginUser.getId(), aiText != null ? aiText : "", generatedPictureIds, userSessionId,
                        effectiveChatSpaceId);

            // 閻犱礁澧介悿鍡涘传瀹ュ懐瀹?
            response.setChatHistory(aiChatHistory);
            response.setPictureVOs(generatedPictures);
            response.setAiText(aiText);

            log.info("generateAiImage - AI 婵炴垵鐗婃导鍛啅闊厾绠介悗? chatHistoryId: {}, 闁汇垻鍠愰崹姘跺炊閸撗冾暬闁? {}",
                    aiChatHistory.getId(), generatedPictures.size());
            return response;

        } catch (IOException e) {
            log.error("Image processing failed", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Image processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Gemini API 閻犲鍟伴弫銈嗗緞鏉堫偉袝", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini API 閻犲鍟伴弫銈嗗緞鏉堫偉袝:" + e.getMessage());
        }
    }

    /**
     * 闁哄秷顫夊畵?MIME 缂侇偉顕ч悗鐑芥嚔瀹勬澘绲块柡鍌氭矗濞嗐垽骞嶉埡浣烘綌闁?
     */
    private Long getExistingSessionSpaceId(Long sessionId, Long userId) {
        if (sessionId == null) {
            return null;
        }
        ChatHistory sessionAnchor = chatHistoryService.lambdaQuery()
                .eq(ChatHistory::getSessionId, sessionId)
                .eq(ChatHistory::getUserId, userId)
                .orderByAsc(ChatHistory::getId)
                .last("limit 1")
                .one();
        ThrowUtils.throwIf(sessionAnchor == null, ErrorCode.NOT_FOUND_ERROR, "Chat session not found");
        return sessionAnchor.getSpaceId();
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".png";
        }
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }

    /**
     * 濞?URL 濞戞挸顑堝ù鍥炊閸撗冾暬
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * 閻忓繐妫楅悺褔鎳為崒娑欐缂備礁瀚ù鍡涘箲椤叀绀?MultipartFile 闁汇劌瀚悿鍕偝閹殿喛顫?
     * 闁活潿鍔嬬花顒備焊?Gemini 閺夆晜鏌ㄥú鏍儍閸曨偅绂堥柣妤€娲﹂弳鐔煎箲椤旀寧绁柟璇℃線鐠愮喖宕ｉ娆戠憪濞磋偐濮撮崺?COS 闁汇劌瀚悧绋款嚕?
     */
    private static class Base64DecodedMultipartFile implements MultipartFile {

        private final byte[] content;
        private final String fileName;
        private final String contentType;

        public Base64DecodedMultipartFile(byte[] content, String fileName, String contentType) {
            this.content = content;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
