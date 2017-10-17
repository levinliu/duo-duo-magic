package org.duoduochild.magic.magiccrawler.datainput;

import com.google.common.annotations.VisibleForTesting;
import org.duoduochild.magic.magiccrawler.dataflow.datainput.GuessEduEntityName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by levinliu on 2017/10/14
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
@RunWith(Parameterized.class)
public class GuessEduEntityNameTest {
    private String entityName;
    private String title;
    private String description;

    public GuessEduEntityNameTest(String entityName, String title, String description) {
        this.entityName = entityName;
        this.title = title;
        this.description = description;
    }

    @Parameterized.Parameters(name = "{index}: guess({0}-{1})")
    public static Collection usernameData() {
        return Arrays.asList(new Object[][]{
                {"南昌少儿培训", "南昌少儿培训_提供南昌少儿英语,美术,舞蹈等少儿培训信息..._地宝网", "教育培训 搜索 首页 培训 技能培训 外语培训 IT培训 艺术培训 少儿培训 学习能力培训 考试 江西高考 学历教育 公务员考试 资格证考试 教育 考研 校园话题 家长会..."},
                {"少儿培训", "少儿培训,少儿培训机构", "少儿培训是许多年轻家长关注的话题,少儿培训机构哪家好?这些更是茶余饭后讨论的热门首选。 少儿培训机构需要考察的东西要比成人培训机构多很多。"},
                {"哈哈儿童", "哈哈儿童-中国领先的课外素质教育活动平台", "在哈哈儿童发现最精彩、最好玩、最刺激的亲子内容,每周500+场亲子活动、300+票务演出、10000+学习资料和教育资源、5000+儿童服务机构。"}});
    }

    @Test
    public void testGuessName() {
        GuessEduEntityName guess = new GuessEduEntityName(title, description);
        Optional<String> actual = guess.guessName();
        if (actual.isPresent()) {
            assertThat(actual.get()).isEqualTo(entityName);
        }
    }
}
