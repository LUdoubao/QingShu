package org.doubao.favorite.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.favorite.service.entity.FavoriteFolder;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {
	List<FavoriteFolder> selectUserFolders(Long userId);
}