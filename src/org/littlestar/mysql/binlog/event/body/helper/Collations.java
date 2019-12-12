package org.littlestar.mysql.binlog.event.body.helper;

import java.util.HashMap;

public class Collations {
	static final HashMap<Integer, Collation> collations = new HashMap<Integer, Collation>();
	static {
		Collation collation;
		collation = new Collation(1, "big5_chinese_ci", "big5", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(2, "latin2_czech_cs", "latin2", 4);
		collations.put(collation.getId(), collation);
		collation = new Collation(3, "dec8_swedish_ci", "dec8", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(4, "cp850_general_ci", "cp850", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(5, "latin1_german1_ci", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(6, "hp8_english_ci", "hp8", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(7, "koi8r_general_ci", "koi8r", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(8, "latin1_swedish_ci", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(9, "latin2_general_ci", "latin2", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(10, "swe7_swedish_ci", "swe7", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(11, "ascii_general_ci", "ascii", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(12, "ujis_japanese_ci", "ujis", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(13, "sjis_japanese_ci", "sjis", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(14, "cp1251_bulgarian_ci", "cp1251", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(15, "latin1_danish_ci", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(16, "hebrew_general_ci", "hebrew", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(18, "tis620_thai_ci", "tis620", 4);
		collations.put(collation.getId(), collation);
		collation = new Collation(19, "euckr_korean_ci", "euckr", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(20, "latin7_estonian_cs", "latin7", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(21, "latin2_hungarian_ci", "latin2", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(22, "koi8u_general_ci", "koi8u", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(23, "cp1251_ukrainian_ci", "cp1251", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(24, "gb2312_chinese_ci", "gb2312", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(25, "greek_general_ci", "greek", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(26, "cp1250_general_ci", "cp1250", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(27, "latin2_croatian_ci", "latin2", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(28, "gbk_chinese_ci", "gbk", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(29, "cp1257_lithuanian_ci", "cp1257", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(30, "latin5_turkish_ci", "latin5", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(31, "latin1_german2_ci", "latin1", 2);
		collations.put(collation.getId(), collation);
		collation = new Collation(32, "armscii8_general_ci", "armscii8", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(33, "utf8_general_ci", "utf8", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(34, "cp1250_czech_cs", "cp1250", 2);
		collations.put(collation.getId(), collation);
		collation = new Collation(35, "ucs2_general_ci", "ucs2", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(36, "cp866_general_ci", "cp866", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(37, "keybcs2_general_ci", "keybcs2", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(38, "macce_general_ci", "macce", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(39, "macroman_general_ci", "macroman", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(40, "cp852_general_ci", "cp852", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(41, "latin7_general_ci", "latin7", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(42, "latin7_general_cs", "latin7", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(43, "macce_bin", "macce", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(44, "cp1250_croatian_ci", "cp1250", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(45, "utf8mb4_general_ci", "utf8mb4", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(46, "utf8mb4_bin", "utf8mb4", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(47, "latin1_bin", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(48, "latin1_general_ci", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(49, "latin1_general_cs", "latin1", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(50, "cp1251_bin", "cp1251", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(51, "cp1251_general_ci", "cp1251", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(52, "cp1251_general_cs", "cp1251", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(53, "macroman_bin", "macroman", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(54, "utf16_general_ci", "utf16", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(55, "utf16_bin", "utf16", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(56, "utf16le_general_ci", "utf16le", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(57, "cp1256_general_ci", "cp1256", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(58, "cp1257_bin", "cp1257", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(59, "cp1257_general_ci", "cp1257", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(60, "utf32_general_ci", "utf32", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(61, "utf32_bin", "utf32", 1);
		collations.put(collation.getId(), collation);
		collation = new Collation(62, "utf16le_bin", "utf16le", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(63, "binary", "binary", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(64, "armscii8_bin", "armscii8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(65, "ascii_bin", "ascii", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(66, "cp1250_bin", "cp1250", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(67, "cp1256_bin", "cp1256", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(68, "cp866_bin", "cp866", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(69, "dec8_bin", "dec8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(70, "greek_bin", "greek", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(71, "hebrew_bin", "hebrew", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(72, "hp8_bin", "hp8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(73, "keybcs2_bin", "keybcs2", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(74, "koi8r_bin", "koi8r", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(75, "koi8u_bin", "koi8u", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(76, "utf8_tolower_ci", "utf8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(77, "latin2_bin", "latin2", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(78, "latin5_bin", "latin5", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(79, "latin7_bin", "latin7", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(80, "cp850_bin", "cp850", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(81, "cp852_bin", "cp852", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(82, "swe7_bin", "swe7", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(83, "utf8_bin", "utf8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(84, "big5_bin", "big5", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(85, "euckr_bin", "euckr", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(86, "gb2312_bin", "gb2312", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(87, "gbk_bin", "gbk", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(88, "sjis_bin", "sjis", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(89, "tis620_bin", "tis620", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(90, "ucs2_bin", "ucs2", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(91, "ujis_bin", "ujis", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(92, "geostd8_general_ci", "geostd8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(93, "geostd8_bin", "geostd8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(94, "latin1_spanish_ci", "latin1", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(95, "cp932_japanese_ci", "cp932", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(96, "cp932_bin", "cp932", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(97, "eucjpms_japanese_ci", "eucjpms", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(98, "eucjpms_bin", "eucjpms", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(99, "cp1250_polish_ci", "cp1250", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(101, "utf16_unicode_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(102, "utf16_icelandic_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(103, "utf16_latvian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(104, "utf16_romanian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(105, "utf16_slovenian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(106, "utf16_polish_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(107, "utf16_estonian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(108, "utf16_spanish_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(109, "utf16_swedish_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(110, "utf16_turkish_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(111, "utf16_czech_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(112, "utf16_danish_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(113, "utf16_lithuanian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(114, "utf16_slovak_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(115, "utf16_spanish2_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(116, "utf16_roman_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(117, "utf16_persian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(118, "utf16_esperanto_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(119, "utf16_hungarian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(120, "utf16_sinhala_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(121, "utf16_german2_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(122, "utf16_croatian_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(123, "utf16_unicode_520_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(124, "utf16_vietnamese_ci", "utf16", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(128, "ucs2_unicode_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(129, "ucs2_icelandic_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(130, "ucs2_latvian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(131, "ucs2_romanian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(132, "ucs2_slovenian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(133, "ucs2_polish_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(134, "ucs2_estonian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(135, "ucs2_spanish_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(136, "ucs2_swedish_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(137, "ucs2_turkish_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(138, "ucs2_czech_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(139, "ucs2_danish_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(140, "ucs2_lithuanian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(141, "ucs2_slovak_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(142, "ucs2_spanish2_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(143, "ucs2_roman_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(144, "ucs2_persian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(145, "ucs2_esperanto_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(146, "ucs2_hungarian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(147, "ucs2_sinhala_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(148, "ucs2_german2_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(149, "ucs2_croatian_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(150, "ucs2_unicode_520_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(151, "ucs2_vietnamese_ci", "ucs2", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(159, "ucs2_general_mysql500_ci", "ucs2", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(160, "utf32_unicode_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(161, "utf32_icelandic_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(162, "utf32_latvian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(163, "utf32_romanian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(164, "utf32_slovenian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(165, "utf32_polish_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(166, "utf32_estonian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(167, "utf32_spanish_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(168, "utf32_swedish_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(169, "utf32_turkish_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(170, "utf32_czech_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(171, "utf32_danish_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(172, "utf32_lithuanian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(173, "utf32_slovak_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(174, "utf32_spanish2_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(175, "utf32_roman_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(176, "utf32_persian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(177, "utf32_esperanto_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(178, "utf32_hungarian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(179, "utf32_sinhala_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(180, "utf32_german2_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(181, "utf32_croatian_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(182, "utf32_unicode_520_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(183, "utf32_vietnamese_ci", "utf32", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(192, "utf8_unicode_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(193, "utf8_icelandic_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(194, "utf8_latvian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(195, "utf8_romanian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(196, "utf8_slovenian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(197, "utf8_polish_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(198, "utf8_estonian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(199, "utf8_spanish_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(200, "utf8_swedish_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(201, "utf8_turkish_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(202, "utf8_czech_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(203, "utf8_danish_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(204, "utf8_lithuanian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(205, "utf8_slovak_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(206, "utf8_spanish2_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(207, "utf8_roman_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(208, "utf8_persian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(209, "utf8_esperanto_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(210, "utf8_hungarian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(211, "utf8_sinhala_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(212, "utf8_german2_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(213, "utf8_croatian_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(214, "utf8_unicode_520_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(215, "utf8_vietnamese_ci", "utf8", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(223, "utf8_general_mysql500_ci", "utf8", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(224, "utf8mb4_unicode_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(225, "utf8mb4_icelandic_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(226, "utf8mb4_latvian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(227, "utf8mb4_romanian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(228, "utf8mb4_slovenian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(229, "utf8mb4_polish_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(230, "utf8mb4_estonian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(231, "utf8mb4_spanish_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(232, "utf8mb4_swedish_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(233, "utf8mb4_turkish_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(234, "utf8mb4_czech_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(235, "utf8mb4_danish_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(236, "utf8mb4_lithuanian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(237, "utf8mb4_slovak_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(238, "utf8mb4_spanish2_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(239, "utf8mb4_roman_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(240, "utf8mb4_persian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(241, "utf8mb4_esperanto_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(242, "utf8mb4_hungarian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(243, "utf8mb4_sinhala_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(244, "utf8mb4_german2_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(245, "utf8mb4_croatian_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(246, "utf8mb4_unicode_520_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(247, "utf8mb4_vietnamese_ci", "utf8mb4", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(248, "gb18030_chinese_ci", "gb18030", 2);
		collations.put(collation.getId(), collation);

		collation = new Collation(249, "gb18030_bin", "gb18030", 1);
		collations.put(collation.getId(), collation);

		collation = new Collation(250, "gb18030_unicode_520_ci", "gb18030", 8);
		collations.put(collation.getId(), collation);

		collation = new Collation(255, "utf8mb4_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(256, "utf8mb4_de_pb_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(257, "utf8mb4_is_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(258, "utf8mb4_lv_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(259, "utf8mb4_ro_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(260, "utf8mb4_sl_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(261, "utf8mb4_pl_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(262, "utf8mb4_et_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(263, "utf8mb4_es_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(264, "utf8mb4_sv_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(265, "utf8mb4_tr_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(266, "utf8mb4_cs_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(267, "utf8mb4_da_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(268, "utf8mb4_lt_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(269, "utf8mb4_sk_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(270, "utf8mb4_es_trad_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(271, "utf8mb4_la_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(273, "utf8mb4_eo_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(274, "utf8mb4_hu_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(275, "utf8mb4_hr_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(277, "utf8mb4_vi_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(278, "utf8mb4_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(279, "utf8mb4_de_pb_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(280, "utf8mb4_is_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(281, "utf8mb4_lv_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(282, "utf8mb4_ro_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(283, "utf8mb4_sl_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(284, "utf8mb4_pl_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(285, "utf8mb4_et_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(286, "utf8mb4_es_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(287, "utf8mb4_sv_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(288, "utf8mb4_tr_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(289, "utf8mb4_cs_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(290, "utf8mb4_da_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(291, "utf8mb4_lt_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(292, "utf8mb4_sk_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(293, "utf8mb4_es_trad_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(294, "utf8mb4_la_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(296, "utf8mb4_eo_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(297, "utf8mb4_hu_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(298, "utf8mb4_hr_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(300, "utf8mb4_vi_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(303, "utf8mb4_ja_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(304, "utf8mb4_ja_0900_as_cs_ks", "utf8mb4", 24);
		collations.put(collation.getId(), collation);

		collation = new Collation(305, "utf8mb4_0900_as_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(306, "utf8mb4_ru_0900_ai_ci", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(307, "utf8mb4_ru_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(308, "utf8mb4_zh_0900_as_cs", "utf8mb4", 0);
		collations.put(collation.getId(), collation);

		collation = new Collation(309, "utf8mb4_0900_bin", "utf8mb4", 1);
		collations.put(collation.getId(), collation);

	}
	
	public static Collation getCollation(int id) {
		return collations.get(id);
	}

}
